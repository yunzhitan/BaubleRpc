package top.yunzhitan.rpc;

import top.yunzhitan.Util.SocketAddress;
import top.yunzhitan.registry.NotifyListener;
import top.yunzhitan.registry.RegisterMeta;
import top.yunzhitan.registry.Registry;
import top.yunzhitan.registry.RegistryService;
import top.yunzhitan.transport.Directory;

import java.util.Collection;

public interface Client extends Registry{

    /**
     * 每一个应用都建议设置一个appName.
     */
    String appName();
    /**
     * 注册服务实例
     */
    RegistryService registryService();

    /**
     * 从本地容器查找服务信息.
     */
    Collection<RegisterMeta> lookup(Directory directory);

    /**
     * 设置对指定服务由jupiter自动管理连接.
     */
    JConnector.ConnectionWatcher watchConnections(Class<?> interfaceClass);

    /**
     * 设置对指定服务由jupiter自动管理连接.
     */
    JConnector.ConnectionWatcher watchConnections(Class<?> interfaceClass, String version);

    /**
     * 设置对指定服务由jupiter自动管理连接.
     */
    JConnector.ConnectionWatcher watchConnections(Directory directory);

    /**
     * 阻塞等待一直到该服务有可用连接或者超时.
     */
    boolean awaitConnections(Class<?> interfaceClass, long timeoutMillis);

    /**
     * 阻塞等待一直到该服务有可用连接或者超时.
     */
    boolean awaitConnections(Class<?> interfaceClass, String version, long timeoutMillis);

    /**
     * 阻塞等待一直到该服务有可用连接或者超时.
     */
    boolean awaitConnections(Directory directory, long timeoutMillis);

    /**
     * 从注册中心订阅一个服务.
     */
    void subscribe(Directory directory, NotifyListener listener);

    /**
     * 服务下线通知.
     */
    void offlineListening(SocketAddress address, OfflineListener listener);

    /**
     * 优雅关闭jupiter client.
     */
    void shutdownGracefully();

}
