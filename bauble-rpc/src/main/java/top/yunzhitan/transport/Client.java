package top.yunzhitan.transport;

import top.yunzhitan.common.ServiceConfig;
import top.yunzhitan.rpc.model.ProviderConfig;
import top.yunzhitan.registry.Registry;
import top.yunzhitan.transport.processor.ClientProcessor;

import java.net.SocketAddress;
import java.util.concurrent.CopyOnWriteArrayList;

public interface Client extends Registry{
    /**
     * Returns the rpc processor.
     */
    ClientProcessor getProcessor();

    String getAppName();

    /**
     * 初始化
     */
    void init();

    /**
     * Binds the rpc processor.
     */
    void setProcessor(ClientProcessor processor);

    /**
     * Connects to the remote peer.
     */
    void writeMessage(SocketAddress address, RequestMessage message, FutureListener listener);

    /**
     * try to writeMessage to the remote peer.
     * @param address
     * @param listener
     */
    void tryConnect(SocketAddress address,FutureListener listener);

    void writeMessage(SocketAddress address, RequestMessage message, boolean async, FutureListener listener);


    /**
     * Shutdown the server.
     */
    void shutdownGracefully();

    boolean addRemotePeer(ServiceConfig serviceConfig, RemotePeer remotePeer);

    boolean removeRemotePeer(ServiceConfig serviceConfig, RemotePeer remotePeer);

    RemotePeer getRemotePeer(ProviderConfig providerConfig);

    boolean isServiceAvalible(ServiceConfig serviceConfig);

    CopyOnWriteArrayList<RemotePeer> getRemotePeerList(ServiceConfig serviceConfig);

    void setConsumer(Class<?> interfaceClass, String version);

    boolean waitForAvailable(long timeoutMillis);

}
