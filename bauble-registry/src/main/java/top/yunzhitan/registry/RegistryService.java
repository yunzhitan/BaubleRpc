package top.yunzhitan.registry;

import top.yunzhitan.rpc.model.Service;

import java.util.Collection;
import java.util.Map;

/**
 * 将服务注册到注册中心 服务器与注册中心的交互类
 */

public interface RegistryService extends Registry {

    /**
     * register the service to the registry server
     * @param registryConfig
     */
    void register(RegistryConfig registryConfig);

    /**
     * unregister the service to the registry server
     * @param registryConfig
     */
    void unRegister(RegistryConfig registryConfig);

    void subscribe(Service registry, NotifyListener listener);

    /**
     * lookup a service in the local scope
     * @param metadata
     * @return
     */
    Collection<RegistryConfig> lookup(Service metadata);

    /**
     * list of all the consumers
     * @return
     */
    Map<Service,Integer> getConsumers();

    /**
     * List of the providers
     */
    Map<RegistryConfig,RegistryState> getProviders();

    /**
     * Return true if the RegidtryService is shutdown
     * @return
     */
    boolean isShutdown();

    /**
     * shutdown
     */
    void shutdownGracefully();

    void destroy();
}
