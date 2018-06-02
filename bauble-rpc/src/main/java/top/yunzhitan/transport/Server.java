package top.yunzhitan.transport;


import top.yunzhitan.common.ServiceConfig;
import top.yunzhitan.registry.RegistryType;
import top.yunzhitan.rpc.provider.Provider;
import top.yunzhitan.transport.processor.ServerProcessor;

import java.net.SocketAddress;

/**
 * BaubleRpc服务器接口
 */

public interface Server {

    /**
     * local address
     * @return
     */
    SocketAddress getLocalAddress();

    void setLocalAddress(SocketAddress address);

    /**
     * 服务器启动之前的初始化工作 需要设置服务器地址
     */
    void init();

    /**
     * start the server
     */
    void start() throws InterruptedException;

    /**
     * shutdown the server gracefully
     */
    void shutdownGracefully();

    void setProcessor(ServerProcessor processor);

    ServerProcessor getRequestProcessor();

    Provider findServiceProvider(ServiceConfig serviceConfig);

    /**
     * 发布指定服务到注册中心.
     */
    void publish(Provider provider);

    /**
     * 从注册中心把指定服务下线.
     */
    void unpublish(Provider provider);

    void setRegistryType(RegistryType registryType);

    RegistryType getRegistryType();

    void connectRegistryServer(String registryConfig);

}
