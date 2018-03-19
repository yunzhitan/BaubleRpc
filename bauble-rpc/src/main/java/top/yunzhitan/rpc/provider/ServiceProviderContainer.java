package top.yunzhitan.rpc.provider;

import top.yunzhitan.rpc.model.ServiceProvider;

import java.util.List;

public interface ServiceProviderContainer {

    /**
     * 注册服务到本地容器
     * @param uniqueKey
     * @param serviceProvider
     */
    void registerService(String uniqueKey, ServiceProvider serviceProvider);

    /**
     * 在本地容器查找服务
     */
    ServiceProvider findService(String uniqueKey);

    /**
     * 从本地容器移除服务
     * @param uniqueKey
     */
    ServiceProvider removeService(String uniqueKey);

    /**
     * 从本地容器中获取所有服务
     * @return
     */
    List<ServiceProvider> getAllService();
}
