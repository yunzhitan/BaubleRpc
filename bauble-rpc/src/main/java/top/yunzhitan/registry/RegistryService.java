package top.yunzhitan.registry;

import top.yunzhitan.common.ServiceConfig;
import top.yunzhitan.rpc.model.ProviderConfig;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 将服务注册到注册中心 服务器与注册中心的交互类
 */

public interface RegistryService extends Registry {

    /**
     * addService the serviceConfig to the top.yunzhitan.registry server
     * @param providerConfig
     */
    void register(ProviderConfig providerConfig);

    /**
     * unregister the serviceConfig to the top.yunzhitan.registry server
     * @param providerConfig
     */
    void unRegister(ProviderConfig providerConfig);

    List<ProviderConfig> subscribe(ServiceConfig registry, NotifyListener listener);

    /**
     * lookup a serviceConfig in the local scope
     * @param metadata
     * @return
     */
    Collection<ProviderConfig> lookup(ServiceConfig metadata);

    /**
     * list of all the consumers
     * @return
     */
    Map<ServiceConfig,Integer> getConsumers();

    /**
     * List of the providers
     */
    Map<ProviderConfig,RegistryState> getProviders();

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

    /**
     * 初始化和开始操作
     */
    void start(String addressConfig);
}
