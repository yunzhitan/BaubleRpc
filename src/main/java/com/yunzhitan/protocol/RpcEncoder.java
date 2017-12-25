package com.yunzhitan.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.EncoderException;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcEncoder extends MessageToByteEncoder {

    private final RpcSerialize rpcSerialize;
    private final Class<?> genericClass;
    private static final Logger logger = LoggerFactory.getLogger(RpcEncoder.class);

    public RpcEncoder(Class<?> genericClass,RpcSerialize serialize) {
        this.genericClass = genericClass;
        this.rpcSerialize = serialize;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf out) throws Exception {
        try {
            if (genericClass.isInstance(msg)) {
                byte[] data = rpcSerialize.serialize(msg);
                out.writeInt(data.length);
                out.writeBytes(data);
            }
        } catch (EncoderException e) {
            logger.error("Find encoderException!");
        }
    }
}
