package com.yunzhitan.client;

import com.yunzhitan.model.RpcRequest;
import com.yunzhitan.model.RpcResponse;
import com.yunzhitan.protocol.RpcDecoder;
import com.yunzhitan.protocol.RpcEncoder;
import com.yunzhitan.protocol.RpcProtocal;
import com.yunzhitan.protocol.RpcProtocalPool;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;


public class RpcConsumerInitializer extends ChannelInitializer<SocketChannel>
{
    private final RpcProtocal protocal;

    public RpcConsumerInitializer(RpcProtocal protocal) {
        this.protocal = protocal;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) {
        ChannelPipeline cp = socketChannel.pipeline();
        cp.addLast(new RpcEncoder(RpcRequest.class, RpcProtocalPool.getInstance().getProtocal(protocal)));
        cp.addLast(new LengthFieldBasedFrameDecoder(1<<16, 0, 4, 0, 0));
        cp.addLast(new RpcDecoder(RpcResponse.class, RpcProtocalPool.getInstance().getProtocal(protocal)));
        cp.addLast(new ConsumerHandler());
    }
}
