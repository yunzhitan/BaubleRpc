package top.yunzhitan.transport.netty.handler;

import top.yunzhitan.transport.RequestMessage;
import top.yunzhitan.transport.ResponseMessage;
import top.yunzhitan.transport.ProtocolHeader;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 传输层协议头
 *
 * **************************************************************************************************
 *                                          Protocol
 *  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
 *       2   │   1   │    1   │     8     │      4      │
 *  ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
 *           │       │        │           │             │
 *  │  MAGIC   Sign    Status   Invoke Id   Body Length                   Body Content              │
 *           │       │        │           │             │
 *  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
 *
 **/

@ChannelHandler.Sharable
public class ProtocolEncoder extends MessageToByteEncoder {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        if(o instanceof RequestMessage) {
            encodeRequest((RequestMessage)o,byteBuf);
        } else if(o instanceof ResponseMessage) {
            encodeResponse((ResponseMessage) o,byteBuf);
        } else {
            throw new IllegalArgumentException();
        }
    }

    private void encodeRequest(RequestMessage requestMessage, ByteBuf byteBuf) {
        byte sign = ProtocolHeader.toSign(requestMessage.serializerCode(),ProtocolHeader.REQUEST);
        byte status = 0x00;
        byte[] bytes = requestMessage.getbytes();
        int length = bytes.length;

        byteBuf.writeShort(ProtocolHeader.MAGIC)
                .writeByte(sign)
                .writeByte(status)
                .writeLong(requestMessage.getRequestId())
                .writeInt(length)
                .writeBytes(bytes);
    }

    private void encodeResponse(ResponseMessage responseMessage, ByteBuf byteBuf){
        byte sign = ProtocolHeader.toSign(responseMessage.serializerCode(),ProtocolHeader.RESPONSE);
        byte status = 0x00;
        byte[] bytes = responseMessage.getbytes();
        int length = bytes.length;

        byteBuf.writeShort(ProtocolHeader.MAGIC)
                .writeByte(sign)
                .writeByte(status)
                .writeLong(responseMessage.getResponseId())
                .writeInt(length)
                .writeBytes(bytes);
    }
}
