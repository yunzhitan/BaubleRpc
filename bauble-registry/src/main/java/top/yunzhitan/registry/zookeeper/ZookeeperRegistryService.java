package top.yunzhitan.registry.zookeeper;

import top.yunzhitan.Util.collection.ConcurrentSet;
import top.yunzhitan.registry.AbstractRegistryService;
import top.yunzhitan.registry.NotifyEvent;
import top.yunzhitan.registry.RegistryConfig;
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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ZookeeperRegistryService extends AbstractRegistryService {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistryService.class);

    private final ConcurrentMap<Service,PathChildrenCache> pathChildrenCaches = new ConcurrentHashMap<>();
    private final ConcurrentMap<SocketAddress,ConcurrentSet<Service>> serviceMetaMap = new ConcurrentHashMap<>();
    private final int sessionTimeoutMs = 60 * 1000;
    private final int connectionTimeoutMs = 15 * 1000;

    private CuratorFramework configClient;



    @Override
    public void doRegister(RegistryConfig registryConfig) {
        String directory = String.format("bauble/provider/%s", registryConfig.getDirectory());

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
                        getRegistered().add(registryConfig);
                    }
                    logger.info("Register: {} - {}", registryConfig,curatorEvent);
                }
            }).forPath(
                    String.format("%s/%s:%s:%s",
                            directory,
                            registryConfig.getHost(),
                            registryConfig.getPort(),
                            registryConfig.getWeight()));
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Create register meta: {} failed, {}", registryConfig,e);
            }
        }
    }

    @Override
    public void doSubscribe(Service service) {
        PathChildrenCache childrenCache = pathChildrenCaches.get(service);
        if (childrenCache == null) {
            String directory = String.format("/bauble/provider/%s",service.getDirectory());
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
                                RegistryConfig registryConfig = RegistryConfig.parseRegistryConfig(event.getData().getPath());
                                SocketAddress address = new InetSocketAddress(registryConfig.getHost(), registryConfig.getPort());
                                Service service = registryConfig.getService();
                                ConcurrentSet<Service> services = serviceMetaMap.get(address);
                                ZookeeperRegistryService.super.notify(
                                        service,
                                        NotifyEvent.CHILD_ADDED,
                                        registryConfig
                                );
                                break;
                            }
                            case CHILD_REMOVED: {
                                RegistryConfig registryConfig = RegistryConfig.parseRegistryConfig(event.getData().getPath());
                                SocketAddress address = new InetSocketAddress(registryConfig.getHost(), registryConfig.getPort());
                                Service service = registryConfig.getService();
                                ConcurrentSet<Service> metaSets = serviceMetaMap.get(address);
                                metaSets.remove(service);
                                ZookeeperRegistryService.super.notify(service,
                                        NotifyEvent.CHILD_REMOVED,
                                        registryConfig);
                            }
                        }
                    }
                });
            }
        }
    }

    @Override
    public void doUnregister(RegistryConfig registryConfig) {
        String directory = String.format("bauble/provider/%s", registryConfig.getDirectory());

        try {
            if(configClient.checkExists().forPath(directory) == null) {
                return;
            }
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Check exists newFuture parent path failed {}-{}", registryConfig,e);
            }
        }

        try {
            configClient.delete().inBackground(new BackgroundCallback() {
                @Override
                public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) {
                    if(curatorEvent.getResultCode() == KeeperException.Code.OK.intValue()) {
                        getRegistered().remove(registryConfig);
                    }
                }
            }).forPath(
                    String.format("%s/%s:%s:%s",
                    directory,
                    registryConfig.getHost(),
                    String.valueOf(registryConfig.getPort()),
                    String.valueOf(registryConfig.getWeight())));
        } catch (Exception e) {
            if(logger.isWarnEnabled()) {
                logger.warn("Delete register meta: {} failed {}", registryConfig,e);
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
                for(RegistryConfig meta: getRegistered()) {
                    doRegister(meta);
                }
                for(Service meta: getSubscribeSet()) {
                    doSubscribe(meta);
                }
            }
        });

        configClient.start();
    }
}
