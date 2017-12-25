package com.yunzhitan.server;

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

class RpcProviderInitializer extends ChannelInitializer<SocketChannel> {

    private final RpcProtocal protocal;

    public RpcProviderInitializer(RpcProtocal protocal) {
        this.protocal = protocal;
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline cp = socketChannel.pipeline();
        cp.addLast(new RpcDecoder(RpcRequest.class, RpcProtocalPool.getInstance().getProtocal(protocal)));
        cp.addLast(new LengthFieldBasedFrameDecoder(1<<30, 0, 4, 0, 0));
        cp.addLast(new RpcEncoder(RpcResponse.class, RpcProtocalPool.getInstance().getProtocal(protocal)));
        cp.addLast(new ProviderHandler());

    }
}
