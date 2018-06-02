package top.yunzhitan.registry.zookeeper;

import top.yunzhitan.Util.SPI;
import top.yunzhitan.Util.collection.ConcurrentSet;
import top.yunzhitan.common.ServiceConfig;
import top.yunzhitan.registry.AbstractRegistryService;
import top.yunzhitan.registry.NotifyEvent;
import top.yunzhitan.registry.ProviderConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.api.CuratorEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.state.ConnectionState;
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

@SPI(name = "zookeeper")
public class ZookeeperRegistryService extends AbstractRegistryService {

    private static final Logger logger = LoggerFactory.getLogger(ZookeeperRegistryService.class);

    private final ConcurrentMap<ServiceConfig,PathChildrenCache> pathChildrenCaches = new ConcurrentHashMap<>();
    private final ConcurrentMap<SocketAddress,ConcurrentSet<ServiceConfig>> serviceMetaMap = new ConcurrentHashMap<>();
    private final int sessionTimeoutMs = 60 * 1000;
    private final int connectionTimeoutMs = 15 * 1000;

    private CuratorFramework configClient;



    @Override
    public void doRegister(ProviderConfig providerConfig) {
        String directory = String.format("/bauble/provider/%s", providerConfig.getDirectory());

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
                        getRegistered().add(providerConfig);
                    }
                    logger.info("Register: {} - {}", providerConfig,curatorEvent);
                }
            }).forPath(
                    String.format("%s/%s:%s:%s",
                            directory,
                            providerConfig.getHost(),
                            providerConfig.getPort(),
                            providerConfig.getWeight()));
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Create addService meta: {} failed, {}", providerConfig,e);
            }
        }
    }

    @Override
    public void doSubscribe(ServiceConfig serviceConfig) {
        PathChildrenCache childrenCache = pathChildrenCaches.get(serviceConfig);
        if (childrenCache == null) {
            String directory = String.format("/bauble/provider/%s", serviceConfig.getDirectory());
            PathChildrenCache newChildrenCache = new PathChildrenCache(configClient, directory, false);
            childrenCache = pathChildrenCaches.putIfAbsent(serviceConfig, newChildrenCache);
            if (childrenCache == null) {
                childrenCache = newChildrenCache;
                childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                    @Override
                    public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) {
                        logger.info("Child event: {} on {}", event,curatorFramework);

                        switch (event.getType()) {
                            case CHILD_ADDED: {
                                ProviderConfig providerConfig = ProviderConfig.parseRegistryConfig(event.getData().getPath());
                                SocketAddress address = new InetSocketAddress(providerConfig.getHost(), providerConfig.getPort());
                                ServiceConfig serviceConfig = providerConfig.getServiceConfig();
                                ConcurrentSet<ServiceConfig> serviceConfigs = serviceMetaMap.computeIfAbsent(address,
                                        k -> new ConcurrentSet<>());
                                serviceConfigs.add(serviceConfig);
                                ZookeeperRegistryService.super.notify(
                                        serviceConfig,
                                        NotifyEvent.CHILD_ADDED,
                                        providerConfig
                                );
                                break;
                            }
                            case CHILD_REMOVED: {
                                ProviderConfig providerConfig = ProviderConfig.parseRegistryConfig(event.getData().getPath());
                                SocketAddress address = new InetSocketAddress(providerConfig.getHost(), providerConfig.getPort());
                                ServiceConfig serviceConfig = providerConfig.getServiceConfig();
                                ConcurrentSet<ServiceConfig> metaSets = serviceMetaMap.get(address);
                                metaSets.remove(serviceConfig);
                                ZookeeperRegistryService.super.notify(serviceConfig,
                                        NotifyEvent.CHILD_REMOVED,
                                        providerConfig);
                            }
                        }
                    }
                });
            try {
                childrenCache.start();
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Subscribe {} failed, {}.", directory, e);
                }
            }
        } else {
            try {
                newChildrenCache.close();
            } catch (IOException e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Close [PathChildrenCache] {} failed, {}.", directory, e);
                }
            }
        }
        }
    }


    @Override
    public void doUnregister(ProviderConfig providerConfig) {
        String directory = String.format("/bauble/provider/%s", providerConfig.getDirectory());

        try {
            if(configClient.checkExists().forPath(directory) == null) {
                return;
            }
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("Check exists newFuture parent path failed {}-{}", providerConfig,e);
            }
        }

        try {
            configClient.delete().inBackground(new BackgroundCallback() {
                @Override
                public void processResult(CuratorFramework curatorFramework, CuratorEvent curatorEvent) {
                    if(curatorEvent.getResultCode() == KeeperException.Code.OK.intValue()) {
                        getRegistered().remove(providerConfig);
                    }
                }
            }).forPath(
                    String.format("%s/%s:%s:%s",
                    directory,
                    providerConfig.getHost(),
                    String.valueOf(providerConfig.getPort()),
                    String.valueOf(providerConfig.getWeight())));
        } catch (Exception e) {
            if(logger.isWarnEnabled()) {
                logger.warn("Delete addService meta: {} failed {}", providerConfig,e);
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
                new ExponentialBackoffRetry(500,20));

        configClient.getConnectionStateListenable().addListener( (curatorFramework,connectionState) ->{
                logger.info("Zookeeper connection state changed {}",connectionState);

                if(connectionState == ConnectionState.RECONNECTED) {
                    logger.info("Zookeeper connection has been re-established");
                    for (ProviderConfig meta : getRegistered()) {
                        doRegister(meta);
                    }
                    for (ServiceConfig meta : getSubscribeSet()) {
                        doSubscribe(meta);
                    }
                }
        });

        configClient.start();
    }

    private PathChildrenCache addPathChildrenCache(CuratorFramework client, String path, boolean cacheData){
        PathChildrenCache childrenCache = new PathChildrenCache(client,path,cacheData);
        childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) {
                logger.info("Child event: {}", event);

                switch (event.getType()) {
                    case CHILD_ADDED: {
                        ProviderConfig providerConfig = ProviderConfig.parseRegistryConfig(event.getData().getPath());
                        SocketAddress address = new InetSocketAddress(providerConfig.getHost(), providerConfig.getPort());
                        ServiceConfig serviceConfig = providerConfig.getServiceConfig();
                        ConcurrentSet<ServiceConfig> serviceConfigs = serviceMetaMap.computeIfAbsent(address,
                                k->new ConcurrentSet<>());
                        serviceConfigs.add(serviceConfig);
                        ZookeeperRegistryService.super.notify(
                                serviceConfig,
                                NotifyEvent.CHILD_ADDED,
                                providerConfig
                        );
                        break;
                    }
                    case CHILD_REMOVED: {
                        ProviderConfig providerConfig = ProviderConfig.parseRegistryConfig(event.getData().getPath());
                        SocketAddress address = new InetSocketAddress(providerConfig.getHost(), providerConfig.getPort());
                        ServiceConfig serviceConfig = providerConfig.getServiceConfig();
                        ConcurrentSet<ServiceConfig> metaSets = serviceMetaMap.get(address);
                        metaSets.remove(serviceConfig);
                        ZookeeperRegistryService.super.notify(serviceConfig,
                                NotifyEvent.CHILD_REMOVED,
                                providerConfig);
                    }

                }
            }
        });
        return childrenCache;
    }
}
