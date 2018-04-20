package top.yunzhitan.registry.zookeeper;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import top.yunzhitan.Util.collection.ConcurrentSet;
import top.yunzhitan.registry.AbstructRegistryService;
import top.yunzhitan.registry.NotifyEvent;
import top.yunzhitan.registry.URL;
import top.yunzhitan.rpc.model.Service;
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

import java.io.IOException;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ZookeeperRegistryService extends AbstructRegistryService {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistryService.class);

    private final ConcurrentMap<Service,PathChildrenCache> pathChildrenCaches = new ConcurrentHashMap<>();
    private final ConcurrentMap<SocketAddress,ConcurrentSet<Service>> serviceMetaMap = new ConcurrentHashMap<>();
    private final int sessionTimeoutMs = 60 * 1000;
    private final int connectionTimeoutMs = 15 * 1000;

    private CuratorFramework configClient;



    @Override
    public void doRegister(URL URL) {
        String directory = String.format("bauble/provider/%s%s%s",
                URL.getGroup(),
                URL.getService(),
                URL.getVersion());

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
                public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) {
                    if (curatorEvent.getResultCode() == KeeperException.Code.OK.intValue()) {
                        getURLSet().add(URL);
                    }
                    logger.info("Register: {} - {}", URL,curatorEvent);
                }
            }).forPath(
                    String.format("%s/%s:%s:%s",
                            directory,
                            URL.getHost(),
                            URL.getPort(),
                            URL.getWeight()));
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Create register meta: {} failed, {}", URL,e);
            }
        }
    }

    @Override
    public void doSubscribe(Service service) {
        PathChildrenCache childrenCache = pathChildrenCaches.get(service);
        if (childrenCache == null) {
            String directory = String.format("/bauble/provider/%s/%s/%s",
                    service.getGroup(),
                    service.getServiceName(),
                    service.getVersion());
            PathChildrenCache newChildrenCache = new PathChildrenCache(configClient,directory,false);
            childrenCache = pathChildrenCaches.putIfAbsent(service,newChildrenCache);

            if(childrenCache == null) {  //put成功
                childrenCache = newChildrenCache;
                childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) {
                        logger.info("Child event: {}", event);

                        switch (event.getType()) {
                            case CHILD_ADDED: {
                                URL URL = top.yunzhitan.registry.URL.parseURL(event.getData().getPath());
                                SocketAddress address = URL.getAddress();
                                Service service = URL.getService();
                                ConcurrentSet<Service> services = serviceMetaMap.get(address);
                                ZookeeperRegistryService.super.notify(
                                        service,
                                        NotifyEvent.CHILD_ADDED,
                                        URL
                                );
                                break;
                            }
                            case CHILD_REMOVED: {
                                URL URL = top.yunzhitan.registry.URL.parseURL(event.getData().getPath());
                                SocketAddress address = URL.getAddress();
                                Service service = URL.getService();
                                ConcurrentSet<Service> metaSets = serviceMetaMap.get(address);
                                metaSets.remove(service);
                                ZookeeperRegistryService.super.notify(service,
                                        NotifyEvent.CHILD_REMOVED,
                                        URL);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void doUnregister(URL URL) {
        String directory = String.format("bauble/provider/%s%s%s",
                URL.getGroup(),
                URL.getService(),
                URL.getVersion());

        try {
            if(configClient.checkExists().forPath(directory) == null) {
                return;
            }
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Check exists with parent path failed {}-{}", URL,e);
            }
        }

        try {
            configClient.delete().inBackground(new BackgroundCallback() {
                @Override
                public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) {
                    if(curatorEvent.getResultCode() == KeeperException.Code.OK.intValue()) {
                        getURLSet().remove(URL);
                    }
                }
            }).forPath(
                    String.format("%s/%s:%s:%s",
                    directory,
                    URL.getHost(),
                    String.valueOf(URL.getPort()),
                    String.valueOf(URL.getWeight())));
        } catch (Exception e) {
            if(logger.isWarnEnabled()) {
                logger.warn("Delete register meta: {} failed {}", URL,e);
            }
        }
    }

    @Override
    public void destroy() {
        for (PathChildrenCache childrenCache : pathChildrenCaches.values()) {
            try {
                childrenCache.close();
            } catch (IOException ignored) {}
        }

        configClient.close();
    }

    public List<Service> findServiceMetaByAddress(SocketAddress address) {
        return Lists.transform(
                Lists.newArrayList(getServiceMeta(address)),
                new Function<Service,Service>() {

                    @Override
                    public Service apply(Service input) {
                        Service copy = new Service();
                        copy.setGroup(input.getGroup());
                        copy.setServiceName(input.getServiceName());
                        copy.setVersion(input.getVersion());
                        return copy;
                    }
                });
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
                for(URL meta: getURLSet()) {
                    doRegister(meta);
                }
                for(Service meta: getSubcribeSet()) {
                    doSubscribe(meta);
                }
            }
        });

        configClient.start();
    }
}
