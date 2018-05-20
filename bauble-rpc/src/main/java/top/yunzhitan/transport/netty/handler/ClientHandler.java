package top.yunzhitan.transport.netty.handler;

import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.transport.ResponseMessage;
import top.yunzhitan.transport.processor.ClientProcessor;

public class ClientHandler extends ChannelInboundHandlerAdapter {

    private static Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    private ChannelHandlerContext context;
    private ClientProcessor processor;

    public ClientHandler(ClientProcessor processor) {
        this.processor = processor;
    }

    public ClientHandler() {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Channel ch = ctx.channel();
        if(msg instanceof ResponseMessage) {
            try {
                processor.handleResponse(ch,(ResponseMessage)msg);
            } catch (Exception e) {
                logger.error("An exception was caught: {} on Channel:{}",e,ch);
            }
        } else {
            logger.error("Unknown Response! {} on Channel:{}",msg,ch);
        }
    }

    public ClientProcessor getProcessor() {
        return processor;
    }

    public void setProcessor(ClientProcessor processor) {
        this.processor = processor;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception Caught{} on Channel:{} ",cause,ctx.channel());
    }
}
