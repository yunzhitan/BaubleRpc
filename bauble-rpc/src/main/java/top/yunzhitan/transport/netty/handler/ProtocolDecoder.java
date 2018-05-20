package top.yunzhitan.transport.netty.handler;

import top.yunzhitan.transport.ProtocolHeader;
import top.yunzhitan.transport.RequestMessage;
import top.yunzhitan.transport.ResponseMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * 使用有限状态自动机模型,用来解码消息头的解码器 MAGIC->SIGN->ID->BODY_LENGTH->BODY->MAGIC
 */
public class ProtocolDecoder extends ReplayingDecoder<ProtocolDecoder.State> {

    /**
     * 消息体的最大长度 1<<20
     */
    private static final int MAX_BODY_SIZE = 1<<20;

    private ProtocolHeader header = new ProtocolHeader();

    /**
     * 设置ReplayingDecoder的预备状态为第一个解码标志HEADER_MAGIC
     */
    public ProtocolDecoder() {
        super(State.HEADER_MAGIC);
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        switch (state()) {
            case HEADER_MAGIC:
                checkMagic(byteBuf.readShort());
                checkpoint(State.HEADER_SIGN);
            case HEADER_SIGN:
                header.sign(byteBuf.readByte());
                checkpoint(State.HEADER_STATUS);
            case HEADER_STATUS:
                header.status(byteBuf.readByte());
                checkpoint(State.HEADER_STATUS);
            case HEADER_ID:
                header.id(byteBuf.readLong());
                checkpoint(State.HEADER_BODY_LENGTH);
            case HEADER_BODY_LENGTH:
                header.bodyLength(byteBuf.readInt());
                checkBodyLength(header.bodyLength());
                checkpoint(State.BODY);
            case BODY:
                decodeBody(byteBuf,list);
                checkpoint(State.HEADER_MAGIC);
        }
    }

    private void checkMagic(short magic) {
        if(magic != ProtocolHeader.MAGIC) {
            throw new IllegalArgumentException();
        }
    }

    private void checkBodyLength(int size) {
        if(size > MAX_BODY_SIZE) {
            throw new IllegalArgumentException();
        }
    }

    private void decodeBody(ByteBuf byteBuf,List out) {
        switch (header.messageCode()) {
            case ProtocolHeader.HEARTBEAT:
                break;
            case ProtocolHeader.REQUEST: {
                int length = header.bodyLength();
                byte[] bytes = new byte[length];
                byteBuf.readBytes(bytes);
                RequestMessage requestMessage = new RequestMessage(header.id());
                requestMessage.setTimestamp(System.currentTimeMillis());
                requestMessage.setBytes(bytes);
                requestMessage.setSerializerCode(header.serializerCode());
                out.add(requestMessage);
                break;
            }
            case ProtocolHeader.RESPONSE: {
                int length = header.bodyLength();
                byte[] bytes = new byte[length];
                byteBuf.readBytes(bytes);
                ResponseMessage responseMessage = new ResponseMessage();
                responseMessage.setInvokeId(header.id());
                responseMessage.setSerializerCode(header.serializerCode());
                responseMessage.setStatus(header.status());
                responseMessage.setBytes(bytes);
                out.add(responseMessage);
                break;
            }

            default:
                throw new IllegalArgumentException();
        }
    }

    enum State {
        HEADER_MAGIC,
        HEADER_SIGN,
        HEADER_STATUS,
        HEADER_ID,
        HEADER_BODY_LENGTH,
        BODY
    }
}
