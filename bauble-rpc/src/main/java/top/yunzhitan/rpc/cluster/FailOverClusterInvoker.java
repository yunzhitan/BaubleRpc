package top.yunzhitan.rpc.cluster;

import top.yunzhitan.rpc.consumer.Dispatcher;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.RpcRequest;

/**
 * 失败自动切换, 当出现失败, 重试其它服务器, 要注意的是重试会带来更长的延时.
 *
 * 建议只用于幂等性操作, 通常比较合适用于读操作.
 *
 * 注意failover不能支持广播的调用方式.
 *
 * https://en.wikipedia.org/wiki/Failover
 *
 *
 *
 */

public class FailOverClusterInvoker implements ClusterInvoker {

    private Dispatcher dispatcher;
    private int failoverRetries;

    public FailOverClusterInvoker(Dispatcher dispatcher, int failoverRetries) {
        this.dispatcher = dispatcher;
        this.failoverRetries = failoverRetries;
    }

    @Override
    public ClusterType getClusterType() {
        return ClusterType.FAIL_OVER;
    }

    @Override
    public <T> InvokeFuture<T> invoke(RpcRequest request, Class<T> returnType) throws Exception {
        return dispatcher.dispatch()
    }
}
