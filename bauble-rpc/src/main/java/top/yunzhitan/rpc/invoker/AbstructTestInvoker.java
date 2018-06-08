package top.yunzhitan.rpc.invoker;

import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.rpc.model.RpcResponse;

/**
 * 测试用的Invoker
 */
public abstract class AbstructTestInvoker implements Invoker{

    @Override
    public RpcResponse invoke(RpcRequest request) {
        return null;
    }
}
