package top.yunzhitan.transport.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import lombok.extern.slf4j.Slf4j;
import top.yunzhitan.transport.AbstractChannel;

import java.net.InetSocketAddress;

@Slf4j
public class NettyChannel extends AbstractChannel<ChannelHandlerContext, Channel> {

    private ChannelHandlerContext context;

    private Channel               channel;

    public NettyChannel(Channel channel) {
        this.channel = channel;
    }

    public NettyChannel(ChannelHandlerContext context) {
        this.context = context;
        this.channel = context.channel();
    }

    @Override
    public ChannelHandlerContext channelContext() {
        return context;
    }

    @Override
    public Channel channel() {
        return channel;
    }

    @Override
    public InetSocketAddress remoteAddress() {
        return (InetSocketAddress) channel.remoteAddress();
    }

    @Override
    public InetSocketAddress localAddress() {
        return (InetSocketAddress) channel.localAddress();
    }

    @Override
    public void writeAndFlush(final Object obj) {
        Future future = channel.writeAndFlush(obj);
        future.addListener(new FutureListener() {
            @Override
            public void operationComplete(Future future1) throws Exception {
                if (!future1.isSuccess()) {
                    Throwable throwable = future1.cause();
                    log.error("Failed to send to "
                            + String.format("%s -> %s",channel.localAddress(),channel.remoteAddress())
                            + " for msg : " + obj
                            + ", Cause by:", throwable);
                }
            }
        });
    }

    @Override
    public boolean isAvailable() {
        return channel.isOpen() && channel.isActive();
    }
}
