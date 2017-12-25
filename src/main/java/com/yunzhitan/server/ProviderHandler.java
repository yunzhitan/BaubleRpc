package com.yunzhitan.server;

import com.yunzhitan.model.RpcRequest;
import com.yunzhitan.model.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private static final Logger logger = LoggerFactory.getLogger(ProviderHandler.class);

    @Override
    public void channelRead0(final ChannelHandlerContext ctx,final RpcRequest request) throws Exception {
        logger.debug("receive request",request);
        RpcResponse response = new RpcResponse(request.getRequestId());
        try {
            Object result = RpcServer.handleRequest(request);
            response.setResult(result);
        } catch (Throwable throwable) {
            response.setError(throwable);
            logger.error("",throwable);
        }
        ctx.writeAndFlush(response).addListener((ChannelFutureListener)
                channelFuture -> logger.debug("Send RpcResponse for RpcRequest " + request.getRequestId()));
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("server caught exception", cause);
        ctx.close();
    }
}
