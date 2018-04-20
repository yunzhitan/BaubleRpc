package top.yunzhitan.transport.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import top.yunzhitan.transport.Client;

public class ConnectionListener implements ChannelFutureListener {

    private Client client;

    public ConnectionListener(Client client) {
        this.client = client;
    }

    @Override
    public void operationComplete(ChannelFuture channelFuture) {
        if(!channelFuture.isSuccess()) {
            final EventLoopGroup loopGroup = channelFuture.channel().eventLoop();
            loopGroup.schedule(new Runnable() {
                @Override
                public void run() {
                    client.connect();
                }
            })
        }
    }
}
