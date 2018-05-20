package top.yunzhitan.rpc.filter;

import top.yunzhitan.rpc.invoker.ProviderContext;
import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.rpc.model.ServiceProvider;

public class InvokeFilter implements Filter{
        @Override
        public Type getType() {
            return Type.PROVIDER;
        }

        @Override
        public <T extends FilterContext> void doFilter(RpcRequest request, T filterCtx, FilterChain next) { ProviderContext context = (ProviderContext) filterCtx;

            ServiceProvider provider = ((ProviderContext) filterCtx).getProvider();
            Object invokeResult = provider.executeInvoke(request,(ProviderContext) filterCtx);

            context.setResult(invokeResult);
        }
}
