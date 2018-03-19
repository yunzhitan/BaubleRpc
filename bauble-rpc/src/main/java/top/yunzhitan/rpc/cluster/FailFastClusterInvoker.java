package top.yunzhitan.rpc.cluster;

import top.yunzhitan.rpc.consumer.Dispatcher;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.RpcRequest;
/**
 * 快速失败, 只发起一次调用, 失败立即报错(jupiter缺省设置)
 *
 * 通常用于非幂等性的写操作.
 *
 * https://en.wikipedia.org/wiki/Fail-fast
 *
 * jupiter
 * org.jupiter.rpc.consumer.cluster
 *
 * @author jiachun.fjc
 */

public class FailFastClusterInvoker implements ClusterInvoker {

    private final Dispatcher dispatcher;

    public FailFastClusterInvoker(Dispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public ClusterType getClusterType() {
        return ClusterType.FAIL_FAST;
    }

    @Override
    public <T> InvokeFuture<T> invoke(RpcRequest request, Class<T> returnType) throws Exception {
        return dispatcher.dispatch(request,returnType);
    }
}
