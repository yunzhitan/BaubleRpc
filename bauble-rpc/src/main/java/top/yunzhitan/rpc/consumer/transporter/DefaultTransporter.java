package top.yunzhitan.rpc.consumer.transporter;

import io.netty.channel.Channel;
import top.yunzhitan.rpc.ConsumerHook;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.MethodSpecialConfig;
import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.serialization.Serializer;

import java.util.List;

public class DefaultTransporter implements Transporter {

    private LoadBalancer loadBalancer;
    private Long timeoutMills;
    private Serializer serializer;

    private Channel selectChannel() {
        
    }

    @Override
    public <T> InvokeFuture<T> sendMessage(RpcRequest request, Class<T> returnType) {
        return null;
    }

    @Override
    public Transporter hooks(List<ConsumerHook> hooks) {
        return null;
    }

    @Override
    public Transporter timeoutMillis(long timeoutMillis) {
        return null;
    }

    @Override
    public Transporter methodSpecialConfigs(List<MethodSpecialConfig> methodSpecialConfigs) {
        return null;
    }
}
