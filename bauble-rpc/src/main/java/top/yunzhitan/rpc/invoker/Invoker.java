package top.yunzhitan.rpc.invoker;

import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.rpc.model.RpcResponse;

public interface Invoker {

    /**
     * 执行invoke调用
     * @param request
     * @return
     */
    RpcResponse invoke (RpcRequest request);
}
