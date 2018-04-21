package top.yunzhitan.rpc;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.rpc.future.DefaultInvokeFuture;
import top.yunzhitan.rpc.model.ResultWrapper;
import top.yunzhitan.rpc.model.RpcResponse;
import top.yunzhitan.serialization.Serializer;
import top.yunzhitan.serialization.SerializerFactory;
import top.yunzhitan.transport.RemotePeer;
import top.yunzhitan.transport.ResponseMessage;

public class ClientTask implements Runnable {

    private Logger logger = LoggerFactory.getLogger(ClientTask.class);
    private ResponseMessage message;
    private Channel channel;

    public ClientTask(Channel channel, ResponseMessage message) {
        this.message = message;
        this.channel = channel;
    }

    @Override
    public void run() {
        byte[] objectBytes = message.getBytes();
        Serializer serializer = SerializerFactory.getSerializer(message.getSerializerCode());
        RpcResponse response = new RpcResponse();
        RemotePeer remotePeer = new RemotePeer(channel.remoteAddress());
        ResultWrapper result;
        try {
            result = serializer.readObject(objectBytes,ResultWrapper.class);
            response.setResult(result);
        } catch (Exception e){
            logger.error("Unserialize Error!! {}",e);
            result = new ResultWrapper();
            result.setResult(e);
        }

        response.setResult(result);
        response.setInvokeId(message.getInvokeId());
        response.setStatus(message.getStatus());

        DefaultInvokeFuture.receiveResponse(remotePeer,response);
    }
}
