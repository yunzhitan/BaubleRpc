package top.yunzhitan.rpc;

import top.yunzhitan.common.ServiceConfig;
import top.yunzhitan.registry.NotifyListener;
import top.yunzhitan.rpc.model.ProviderConfig;
import top.yunzhitan.registry.Registry;
import top.yunzhitan.registry.RegistryService;

import java.net.SocketAddress;
import java.util.Collection;

public interface ConnectionManager extends Registry{

    /**
     * 每一个应用都建议设置一个appName.
     */
    String getAppname();
    /**
     * 注册服务实例
     */
    RegistryService getRegistryService();

    /**
     * 从本地容器查找服务信息.
     */
    Collection<ProviderConfig> lookup(ServiceConfig ServiceConfig);

    /**
     * 从注册中心订阅一个服务.
     */
    void subscribe(ServiceConfig ServiceConfig, NotifyListener listener);

    void initialization(ServiceConfig serviceConfig);

    /**
     * 服务下线通知.
     */
    void offlineListening(SocketAddress address, OfflineListener listener);


    void shutdownGracefully();

    boolean waitForAvailable(long timeoutMillis,ServiceConfig serviceConfig);

    void connectRegistryServer(String registryConfig);

}
