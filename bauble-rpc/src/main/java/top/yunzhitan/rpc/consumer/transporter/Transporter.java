package top.yunzhitan.rpc.consumer.transporter;

import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.RpcRequest;

/**
 * 服务消费端将RpcRequest发送到服务提供方的执行类 Transporter应提供负载均衡策略以及传输管理
 */
public interface Transporter {

    <T> InvokeFuture<T> sendMessage(RpcRequest request, Class<T> returnType);


    Transporter timeoutMillis(long timeoutMillis);

}
