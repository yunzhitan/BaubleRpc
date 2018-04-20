package top.yunzhitan.transport.netty.handler;

import top.yunzhitan.transport.RequestMessage;
import top.yunzhitan.transport.Status;
import top.yunzhitan.transport.processor.ServerProcessor;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerHandler extends ChannelInboundHandlerAdapter {
    private static final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    private AtomicInteger channelCounter = new AtomicInteger(0);

    private ServerProcessor processor;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Channel channel = ctx.channel();

        if(msg instanceof RequestMessage) {
            try {
                processor.handleRequest(channel,(RequestMessage) msg);
            } catch (Throwable t) {
                processor.handleException(channel,(RequestMessage)msg, Status.SERVER_ERROR,t);
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        int count = channelCounter.getAndIncrement();
        logger.info("Connects with {} as the {}th channel",ctx.channel(),count);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        int count = channelCounter.getAndDecrement();
        logger.info("Connects with {}, the{} channel has broken down",ctx.channel(),count);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        Channel channel = ctx.channel();

        if(cause instanceof IOException) {
            logger.error("An I/O Exception was caught {} in the channel: {}",cause,channel);
        }
        else {
            logger.error("An UnException was caught {} in the channel: {}",cause,channel);
        }
    }

    public ServerProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(ServerProcessor processor) {
        this.processor = processor;
    }
}
