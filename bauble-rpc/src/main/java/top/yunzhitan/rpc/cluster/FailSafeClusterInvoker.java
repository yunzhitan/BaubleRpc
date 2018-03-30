package top.yunzhitan.rpc.cluster;

import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.RpcRequest;
/**
 * 失败安全, 同步调用时发生异常时只打印日志.
 *
 * 通常用于写入审计日志等操作.
 *
 * http://en.wikipedia.org/wiki/Fail-safe
 *
 * jupiter
 * org.jupiter.rpc.consumer.cluster
 *
 * @author jiachun.fjc
 */

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
    public <T> InvokeFuture<T> invoke(RpcRequest request, Class<T> returnType) throws Exception {
        return null;
    }
}
