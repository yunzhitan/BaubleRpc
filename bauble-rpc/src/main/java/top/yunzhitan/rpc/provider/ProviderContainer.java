package top.yunzhitan.rpc.provider;

import java.util.List;

public interface ProviderContainer {

    /**
     * 注册服务到本地容器
     * @param uniqueKey
     * @param provider
     */
    void addService(String uniqueKey, Provider provider);

    /**
     * 在本地容器查找服务
     */
    Provider findService(String uniqueKey);

    /**
     * 从本地容器移除服务
     * @param uniqueKey
     */
    Provider removeService(String uniqueKey);

    /**
     * 从本地容器中获取所有服务
     * @return
     */
    List<Provider> getAllService();
}
