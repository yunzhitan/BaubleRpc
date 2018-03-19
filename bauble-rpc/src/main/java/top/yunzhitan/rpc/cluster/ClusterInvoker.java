package top.yunzhitan.rpc.cluster;

import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.RpcRequest;

public interface ClusterInvoker {

    ClusterType getClusterType();

    <T> InvokeFuture<T> invoke(RpcRequest request, Class<T> returnType) throws Exception;
}
