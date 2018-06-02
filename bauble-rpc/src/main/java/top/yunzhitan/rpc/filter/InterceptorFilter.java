package top.yunzhitan.rpc.filter;

import top.yunzhitan.rpc.invoker.ProviderContext;
import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.rpc.provider.Provider;
import top.yunzhitan.rpc.provider.ProviderInterceptor;

public class InterceptorFilter implements Filter {

    @Override
    public Type getType() {
        return Type.PROVIDER;
    }

    @Override
    public <T extends FilterContext> void doFilter(RpcRequest request, T filterCtx,FilterChain next) throws Throwable {
        ProviderContext invokeCtx = (ProviderContext) filterCtx;
        Provider provider = ((ProviderContext) filterCtx).getProvider();
        // 拦截器
        ProviderInterceptor[] interceptors = provider.getInterceptors();

        if (interceptors == null || interceptors.length == 0) {
            next.doFilter(request, filterCtx);
        } else {
            String methodName = request.getMethodName();
            Object[] args = request.getArguments();

            handleBeforeInvoke(interceptors, provider, methodName, args);
            try {
                next.doFilter(request, filterCtx);
            } finally {
                handleAfterInvoke(
                        interceptors, provider, methodName, args, invokeCtx.getResult());
            }
        }

    }

    private static void handleBeforeInvoke(ProviderInterceptor[] interceptors,
                                           Object provider,
                                           String methodName,
                                           Object[] args) {

        for (ProviderInterceptor interceptor : interceptors) {
            interceptor.beforeInvoke(provider, methodName, args);
        }
    }

    @SuppressWarnings("all")
    private static void handleAfterInvoke(ProviderInterceptor[] interceptors,
                                          Object provider,
                                          String methodName,
                                          Object[] args,
                                          Object invokeResult) {

        for (int i = interceptors.length - 1; i >= 0; i--) {
                interceptors[i].afterInvoke(provider, methodName, args, invokeResult);
        }
    }

}
