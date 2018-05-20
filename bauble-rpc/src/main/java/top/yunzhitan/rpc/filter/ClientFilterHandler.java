package top.yunzhitan.rpc.filter;

import top.yunzhitan.rpc.invoker.ConsumerContext;
import top.yunzhitan.rpc.model.RpcRequest;

public class ClientFilterHandler {
    private static final FilterChain headFilter;

    static {
        FilterChain clusterFilter = new DefaultFilterChain(new ClusterInvokerFilter(), null);
        headFilter = FilterLoader.loadExtFilters(clusterFilter, Filter.Type.CONSUMER);
    }

    public static void filter(RpcRequest request, ConsumerContext invokeCtx) throws Throwable {
        headFilter.doFilter(request, invokeCtx);
    }

}
