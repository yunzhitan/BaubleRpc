package top.yunzhitan.transport;

import top.yunzhitan.registry.Registry;
import top.yunzhitan.registry.RegistryConfig;
import top.yunzhitan.common.Service;
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

    boolean addRemotePeer(Service service,RemotePeer remotePeer);

    boolean removeRemotePeer(Service service,RemotePeer remotePeer);

    RemotePeer getRemotePeer(RegistryConfig registryConfig);

    boolean isServiceAvalible(Service service);

    CopyOnWriteArrayList<RemotePeer> getRemotePeerList(Service service);

    void setServiceConsumer(Class<?> interfaceClass, String version);

    boolean waitForAvailable(long timeoutMillis);

}
