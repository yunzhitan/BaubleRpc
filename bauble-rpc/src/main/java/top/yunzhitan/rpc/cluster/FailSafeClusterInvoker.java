package top.yunzhitan.rpc.cluster;

import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.RpcRequest;

public class FailSafeClusterInvoker implements ClusterInvoker {

    private Transporter transporter;

    public FailSafeClusterInvoker(Transporter transporter) {
        this.transporter = transporter;
    }

    @Override
    public ClusterType getClusterType() {
        return ClusterType.FAIL_SAFE;
    }

    @Override
    public <T> InvokeFuture<T> invoke(RpcRequest request, Class<T> returnType) {
        return null;
    }
}
