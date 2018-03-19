package top.yunzhitan.rpc;

import top.yunzhitan.rpc.model.ServiceProvider;
import top.yunzhitan.rpc.provider.ProviderInterceptor;

import java.util.concurrent.Executor;

/**
 * 服务注册信息
 */
public interface RegistryLocal {

    /**
     * 设置服务对象
     * @param serviceProvider
     * @return
     */
    RegistryLocal setProvider(Object serviceProvider, ProviderInterceptor... interceptors);

    /**
     * 设置服务接口类型
     * @param interfaceClass
     * @return
     */
    RegistryLocal setInterfaceClass(Class<?> interfaceClass);

    /**
     * 设置服务组别
     * @param group
     * @return
     */
    RegistryLocal setGroup(String group);

    /**
     * 设置服务名称
     * @param providerName
     * @return
     */
    RegistryLocal setProviderName(String providerName);

    /**
     * 设置服务版本号
     * @return
     */
    RegistryLocal setVersion(String version);

    /**
     * 设置服务权重
     * @param weight
     * @return
     */
    RegistryLocal setWeight(int weight);

    /**
     * 设置服务提供者线程池
     * @param executor
     * @return
     */
    RegistryLocal setExecutor(Executor executor);

    /**
     * 注册服务
     * @return
     */
    ServiceProvider register();
}
