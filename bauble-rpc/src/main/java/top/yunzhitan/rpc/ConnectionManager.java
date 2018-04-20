package top.yunzhitan.rpc;

import top.yunzhitan.registry.NotifyListener;
import top.yunzhitan.registry.URL;
import top.yunzhitan.registry.Registry;
import top.yunzhitan.registry.RegistryService;
import top.yunzhitan.rpc.model.Service;
import top.yunzhitan.transport.Directory;
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
    Collection<URL> lookup(Service Service);

    /**
     * 等待直到连接可用
     * @param timeoutMillis
     * @return
     */
    boolean waitForAvailable(long timeoutMillis);

    /**
     * 从注册中心订阅一个服务.
     */
    void subscribe(Service Service, NotifyListener listener);

    ConnectionManager initialization(Class<?> interfaceClass,String version);

    ConnectionManager initialization(Service service);

    /**
     * 服务下线通知.
     */
    void offlineListening(SocketAddress address, OfflineListener listener);


    void shutdownGracefully();

    boolean waitForAvailable(long timeoutMillis,Directory directory);

}
