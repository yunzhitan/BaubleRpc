package top.yunzhitan.registry.zookeeper;

import top.yunzhitan.Util.collection.ConcurrentSet;
import top.yunzhitan.registry.AbstructRegistryService;
import top.yunzhitan.registry.NotifyEvent;
import top.yunzhitan.registry.RegisterMeta;
import top.yunzhitan.rpc.model.ServiceMeta;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ZookeeperRegistryService extends AbstructRegistryService {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistryService.class);

    private final ConcurrentMap<ServiceMeta,PathChildrenCache> pathChildrenCaches = new ConcurrentHashMap<>();
    private final ConcurrentMap<SocketAddress,ConcurrentSet<ServiceMeta>> serviceMetaMap = new ConcurrentHashMap<>();
    private final int sessionTimeoutMs = 60 * 1000;
    private final int connectionTimeoutMs = 15 * 1000;

    private CuratorFramework configClient;



    @Override
    public void doRegister(RegisterMeta registerMeta) {
        String directory = String.format("bauble/provider/%s%s%s",
                registerMeta.getGroup(),
                registerMeta.getServiceMeta(),
                registerMeta.getVersion());

        try {
            if(configClient.checkExists().forPath(directory) == null) {
                configClient.create().creatingParentsIfNeeded().forPath(directory);
            }
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Create parent path failed,directory: {}, {}",directory,e);
            }
        }
        try {
            //znode在client的连接失效后会被删除
            configClient.create().withMode(CreateMode.EPHEMERAL).inBackground(new BackgroundCallback() {
                @Override
                public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                    if (curatorEvent.getResultCode() == KeeperException.Code.OK.intValue()) {
                        getRegisterMetaSet().add(registerMeta);
                    }
                    logger.info("Register: {} - {}",registerMeta,curatorEvent);
                }
            }).forPath(
                    String.format("%s/%s:%s/%s/%s/%s",
                            directory,
                            registerMeta.getHost(),
                            registerMeta.getWeight(),
                            registerMeta.getConnCount()));
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Create register meta: {} failed, {}",registerMeta,e);
            }
        }
    }

    @Override
    public void doSubscribe(ServiceMeta serviceMeta) {
        PathChildrenCache childrenCache = pathChildrenCaches.get(serviceMeta);
        if (childrenCache == null) {
            String directory = String.format("/bauble/provider/%s/%s/%s",
                    serviceMeta.getGroup(),
                    serviceMeta.getServiceName(),
                    serviceMeta.getVersion());
            PathChildrenCache newChildrenCache = new PathChildrenCache(configClient,directory,false);
            childrenCache = pathChildrenCaches.putIfAbsent(serviceMeta,newChildrenCache);

            if(childrenCache == null) {  //put成功
                childrenCache = newChildrenCache;
                childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                        logger.info("Child event: {}", event);

                        switch (event.getType()) {
                            case CHILD_ADDED: {
                                RegisterMeta registerMeta = parseRegisterMeta(event.getData().getPath());
                                SocketAddress address = registerMeta.getAddress();
                                ServiceMeta serviceMeta = registerMeta.getServiceMeta();
                                ConcurrentSet<ServiceMeta> serviceMetas = serviceMetaMap.get(address);
                                ZookeeperRegistryService.super.notify(
                                        serviceMeta,
                                        NotifyEvent.CHILD_ADDED,
                                        registerMeta
                                );
                                break;
                            }
                            case CHILD_REMOVED: {
                                RegisterMeta registerMeta = parseRegisterMeta(event.getData().getPath());
                                SocketAddress address = registerMeta.getAddress();
                                ServiceMeta serviceMeta = registerMeta.getServiceMeta();
                                ConcurrentSet
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void doUnregister(RegisterMeta registerMeta) {
        String directory = String.format("bauble/provider/%s%s%s",
                registerMeta.getGroup(),
                registerMeta.getServiceMeta(),
                registerMeta.getVersion());

        try {
            if(configClient.checkExists().forPath(directory) == null) {
                return;
            }
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Check exists with parent path failed {}-{}",registerMeta,e);
            }
        }

        try {
            configClient.delete().inBackground(new BackgroundCallback() {
                @Override
                public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) throws Exception {
                    if(curatorEvent.getResultCode() == KeeperException.Code.OK.intValue()) {
                        getRegisterMetaSet().remove(registerMeta);
                    }
                }
            }).forPath(
                    String.format("%s/%s:%s:%s:%s",
                    directory,
                    registerMeta.getHost(),
                    String.valueOf(registerMeta.getPort()),
                    String.valueOf(registerMeta.getWeight()),
                    String.valueOf(registerMeta.getConnCount())));
        } catch (Exception e) {
            if(logger.isWarnEnabled()) {
                logger.warn("Delete register meta: {} failed {}",registerMeta,e);
            }
        }
    }


    @Override
    public void connectRegistryServer(String registryConfig) {
        configClient = CuratorFrameworkFactory.newClient(registryConfig,sessionTimeoutMs,connectionTimeoutMs,
                new ExponentialBackoffRetry(500,200));

        configClient.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                logger.info("Zookeeper connection state changed {}",connectionState);

                if(connectionState == ConnectionState.RECONNECTED) {
                    logger.info("Zookeeper connection has been re-established");
                }
                for(RegisterMeta meta: getRegisterMetaSet()) {
                    doRegister(meta);
                }
                for(ServiceMeta meta: getSubcribeSet()) {
                    doSubscribe(meta);
                }
            }
        });

        configClient.start();
    }

    /**
     * data directory/path
     * @param data
     * @return
     */
    private RegisterMeta parseRegisterMeta(String data) {
        //directory
        String[] array_1 = Strings.split(data,'/');
        RegisterMeta meta = new RegisterMeta();
        meta.setGroup(array_1[2]);
        meta.setServiceName(array_1[3]);
        meta.setVersion(array_1[4]);

        //path
        String[] array_2 = Strings.split(array_1[5],':');
        meta.setHost(array_2[0]);
        meta.setPort(Integer.parseInt(array_2[1]));
        meta.setWeight(Integer.parseInt(array_2[2]));
        meta.setConnCount(Integer.parseInt(array_2[3]));

        return meta;
    }
}
