package top.yunzhitan.transport;


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
     * start the server
     */
    void start() throws InterruptedException;

    /**
     * shutdown the server gracefully
     */
    void shutdownGracefully();

    void setProcessor(ServerProcessor processor);

    ServerProcessor getRequestProcessor();
}
