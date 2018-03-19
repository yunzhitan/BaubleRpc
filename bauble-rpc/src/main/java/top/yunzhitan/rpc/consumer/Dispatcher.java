package top.yunzhitan.rpc.consumer;

import top.yunzhitan.rpc.ConsumerHook;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.MethodSpecialConfig;
import top.yunzhitan.rpc.model.RpcRequest;

import java.util.List;

public interface Dispatcher {

    <T> InvokeFuture<T> dispatch(RpcRequest request, Class<T> returnType);

    Dispatcher hooks(List<ConsumerHook> hooks);

    Dispatcher timeoutMillis(long timeoutMillis);

    Dispatcher methodSpecialConfigs(List<MethodSpecialConfig> methodSpecialConfigs);
}
