package top.yunzhitan.rpc.consumer.transporter;

import top.yunzhitan.Util.SocketAddress;
import top.yunzhitan.rpc.consumer.loadbalance.LoadBalancer;
import top.yunzhitan.rpc.future.DefaultInvokeFuture;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.serialization.Serializer;
import top.yunzhitan.transport.Client;
import top.yunzhitan.transport.RemotePeer;
import top.yunzhitan.transport.RequestMessage;

public class DefaultTransporter extends AbstractTransporter {

    private LoadBalancer loadBalancer;

    public DefaultTransporter(Serializer serializer, Client client) {
        super(serializer, client);
    }


    @Override
    public <T> InvokeFuture<T> sendMessage(RpcRequest request, Class<T> returnType) {
        Serializer serializer =  getSerializer();
        RequestMessage message = new RequestMessage();

        RemotePeer remotePeer = loadBalancer.selectPeer();
        byte serialize_code = serializer.code();
        byte[] bytes = serializer.writeObject(request);
        message.setBytes(bytes);

        DefaultInvokeFuture<T> future = DefaultInvokeFuture.with()
    }
}
