package top.yunzhitan.rpc.cluster;

import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.RpcRequest;

public class FailFastClusterInvoker implements ClusterInvoker {

    private final Transporter transporter;

    public FailFastClusterInvoker(Transporter transporter) {
        this.transporter = transporter;
    }

    @Override
    public ClusterType getClusterType() {
        return ClusterType.FAIL_FAST;
    }

    @Override
    public <T> InvokeFuture<T> invoke(RpcRequest request, Class<T> returnType) {
        return transporter.sendMessage(request,returnType);
    }
}
