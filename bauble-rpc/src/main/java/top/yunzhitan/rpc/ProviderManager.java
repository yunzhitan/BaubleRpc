package top.yunzhitan.rpc;

import top.yunzhitan.registry.Registry;
import top.yunzhitan.registry.RegistryService;
import top.yunzhitan.rpc.model.ServiceProvider;
import top.yunzhitan.rpc.provider.ProviderInitializer;
import top.yunzhitan.rpc.provider.ProviderInterceptor;
import top.yunzhitan.transport.Directory;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * 综合管理所有的provider的ProviderManager
 */

public interface ProviderManager extends Registry{

    /**
     * 得到服务发布类
     */
    RegistryService getRegistryService();

    /**
     * 设置全局的拦截器, 会拦截所有的服务提供者.
     */
    void setGlobalInterceptors(ProviderInterceptor... globalInterceptors);

    /**
     * 根据服务目录查找对应服务提供者.
     */
    ServiceProvider findService(Directory directory);

    /**
     * 根据服务目录移除对应服务提供者.
     */
    ServiceProvider removeService(Directory directory);

    /**
     * 注册所有服务到本地容器.
     */
    List<ServiceProvider> allRegisteredServices();

    /**
     * 发布指定服务到注册中心.
     */
    void publish(ServiceProvider serviceProviders);

    /**
     * 发布指定服务列表到注册中心.
     */
    void publish(ServiceProvider... serviceProviders);

    /**
     * 将服务在本地容器注册
     * @param serviceProvider
     */
    void register(ServiceProvider serviceProvider);

    /**
     * 将服务列表在本地容器注册
     * @param serviceProviders
     */
    void register(ServiceProvider... serviceProviders);

    /**
     * 服务提供者初始化完成后再发布服务到注册中心(延迟发布服务).
     */
    <T> void publishWithInitializer(ServiceProvider serviceWrapper, ProviderInitializer<T> initializer);

    /**
     * 服务提供者初始化完成后再发布服务到注册中心(延迟发布服务), 并设置服务私有的线程池来执行初始化操作.
     */
    <T> void publishWithInitializer(ServiceProvider serviceWrapper, ProviderInitializer<T> initializer, Executor executor);

    /**
     * 发布本地所有服务到注册中心.
     */
    void publishAll();

    /**
     * 从注册中心把指定服务下线.
     */
    void unpublish(ServiceProvider serviceWrapper);

    /**
     * 从注册中心把本地所有服务全部下线.
     */
    void unpublishAll();
}
