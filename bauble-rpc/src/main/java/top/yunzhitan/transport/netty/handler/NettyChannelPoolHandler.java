package top.yunzhitan.transport.netty.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.transport.processor.ClientProcessor;

import java.util.concurrent.TimeUnit;

public class NettyChannelPoolHandler implements io.netty.channel.pool.ChannelPoolHandler {

    private static final Logger logger = LoggerFactory.getLogger(NettyChannelPoolHandler.class);
    private ClientProcessor processor;

    public NettyChannelPoolHandler(ClientProcessor processor) {
        this.processor = processor;
    }



    @Override
    public void channelReleased(Channel channel) {
    }

    @Override
    public void channelAcquired(Channel channel) {
    }

    @Override
    public void channelCreated(Channel channel) {
        logger.info("Channel created! {}",channel);

        ChannelPipeline ch = channel.pipeline();
        ch.addLast(new IdleStateHandler(50,0,0, TimeUnit.SECONDS));
        ch.addLast(new ProtocolDecoder());
        ch.addLast(new ProtocolEncoder());
        ch.addLast(new ClientHandler(processor));
    }
}

