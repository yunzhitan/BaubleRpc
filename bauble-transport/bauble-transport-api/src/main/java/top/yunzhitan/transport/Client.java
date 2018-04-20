package top.yunzhitan.transport;

import top.yunzhitan.transport.processor.ClientProcessor;

import java.net.SocketAddress;

public interface Client<C> {
    /**
     * Returns the rpc processor.
     */
    ClientProcessor getProcessor();

    /**
     * Binds the rpc processor.
     */
    void setProcessor(ClientProcessor processor);

    /**
     * Connects to the remote peer.
     */
    C writeMessage(SocketAddress address, RequestMessage message, FutureListener listener);

    /**
     * try to writeMessage to the remote peer.
     * @param address
     * @param listener
     */
    void tryConnect(SocketAddress address,FutureListener listener);

    C writeMessage(SocketAddress address, RequestMessage message, boolean async, FutureListener listener);


    /**
     * Shutdown the server.
     */
    void shutdownGracefully();

    boolean addRemotePeer(Directory directory,RemotePeer remotePeer);

    boolean removeRemotePeer(Directory directory,RemotePeer remotePeer);

    RemotePeer getRemotePeer(SocketAddress address);

    boolean isDirectoryAvailable(Directory directory);

}
