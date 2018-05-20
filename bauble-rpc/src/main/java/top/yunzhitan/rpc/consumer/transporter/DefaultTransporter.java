package top.yunzhitan.rpc.consumer.transporter;

import top.yunzhitan.rpc.consumer.loadbalance.LoadBalancer;
import top.yunzhitan.rpc.future.DefaultInvokeFuture;
import top.yunzhitan.rpc.future.FuturePool;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.serialization.Serializer;
import top.yunzhitan.transport.Client;
import top.yunzhitan.transport.RemotePeer;
import top.yunzhitan.transport.RequestMessage;

public class DefaultTransporter extends AbstractTransporter {


    public DefaultTransporter(Serializer serializer, Client client,LoadBalancer loadBalancer) {
        super(serializer, client, loadBalancer);
    }


    @Override
    public <T> InvokeFuture<T> sendMessage(RpcRequest request, Class<T> returnType) {
        Serializer serializer =  getSerializer();
        RequestMessage message = new RequestMessage();
        RemotePeer remotePeer = select(request.getService());
        remotePeer.addCallCount();

        byte serialize_code = serializer.getCode();
        long startTime = System.currentTimeMillis();
        byte[] bytes = serializer.writeObject(request);
        long endTime = System.currentTimeMillis();
        System.out.println("序列化时间：" + (endTime-startTime));
        long invokeId = message.getInvokeId();
        long timeoutMillis = getTimeoutMills();
        message.setBytes(bytes);
        message.setSerializerCode(serialize_code);
        message.setTimestamp(System.currentTimeMillis());

        DefaultInvokeFuture<T> future = FuturePool.newDefaultFuture(invokeId,returnType,remotePeer
        ,timeoutMillis);

        return write(message,returnType,future,remotePeer);
    }
}
