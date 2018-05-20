package top.yunzhitan.transport;

import io.netty.channel.ChannelFuture;

import java.net.SocketAddress;

public class ConnectionFuture {

    private SocketAddress address;
    private ChannelFuture future;

    public ConnectionFuture(SocketAddress address, ChannelFuture future) {
        this.address = address;
        this.future = future;
    }
}
