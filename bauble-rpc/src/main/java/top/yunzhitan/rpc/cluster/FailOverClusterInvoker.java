package top.yunzhitan.rpc.cluster;

import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.rpc.future.FailOverFuture;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.RpcRequest;


public class FailOverClusterInvoker implements ClusterInvoker {

    private Transporter transporter;
    private int retries;

    public FailOverClusterInvoker(Transporter transporter, int retries) {
        this.transporter = transporter;
        this.retries = retries;
    }

    @Override
    public ClusterType getClusterType() {
        return ClusterType.FAIL_OVER;
    }

    @Override
    public <T> InvokeFuture<T> invoke(RpcRequest request, Class<T> returnType) {
        FailOverFuture failOverFuture = FailOverFuture.newFuture(returnType);
        return transporter.sendMessage(request,returnType);
    }
}
