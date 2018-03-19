package top.yunzhitan.registry;

import com.yunzhitan.rpc.model.ServiceMeta;

import java.util.Collection;
import java.util.Map;

/**
 * 将服务注册到注册中心 服务器与注册中心的交互类
 */

public interface RegistryService extends Registry {

    /**
     * register the service to the registry server
     * @param registry
     */
    void register(RegisterMeta registry);

    /**
     * unregister the service to the registry server
     * @param registry
     */
    void unRegister(RegisterMeta registry);

    void subscribe(ServiceMeta registry);

    /**
     * lookup a service in the local scope
     * @param metadata
     * @return
     */
    Collection<RegisterMeta> lookup(ServiceMeta metadata);

    /**
     * list of all the consumers
     * @return
     */
    Map<ServiceMeta,Integer> getConsumers();

    /**
     * List of the providers
     */
    Map<RegisterMeta,RegistryState> getProviders();

    /**
     * Return true if the RegidtryService is shutdown
     * @return
     */
    boolean isShutdown();

    /**
     * shutdown
     */
    void shutdownGracefully();
}
