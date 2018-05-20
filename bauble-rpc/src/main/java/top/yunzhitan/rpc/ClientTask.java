package top.yunzhitan.rpc;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.rpc.future.FuturePool;
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
            long startTime = System.currentTimeMillis();
            result = serializer.readObject(objectBytes,ResultWrapper.class);
            long endTime = System.currentTimeMillis();
            System.out.println("反序列化时间：" +(endTime-startTime));
            response.setResult(result);
        } catch (Exception e){
            logger.error("Unserialize Error!! {}",e);
            result = new ResultWrapper();
            result.setResult(e);
        }

        response.setInvokeId(message.getInvokeId());
        response.setStatus(message.getStatus());

        FuturePool.receiveResponse(remotePeer,response);
    }
}
