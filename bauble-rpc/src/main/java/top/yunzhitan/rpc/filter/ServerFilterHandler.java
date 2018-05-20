package top.yunzhitan.rpc.filter;

import top.yunzhitan.rpc.model.RpcRequest;

public class ServerFilterHandler {
    private static final FilterChain headChain;

    static {
        FilterChain invokeChain = new DefaultFilterChain(new InvokeFilter(), null);
        FilterChain interceptChain = new DefaultFilterChain(new InterceptorFilter(), invokeChain);
        headChain = FilterLoader.loadExtFilters(interceptChain, Filter.Type.PROVIDER);
    }

    public static <T extends FilterContext> T invoke(RpcRequest request, T invokeCtx) throws Throwable {
        headChain.doFilter(request, invokeCtx);
        return invokeCtx;
    }

}
