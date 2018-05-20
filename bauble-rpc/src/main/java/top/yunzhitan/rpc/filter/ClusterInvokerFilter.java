package top.yunzhitan.rpc.filter;

import top.yunzhitan.rpc.cluster.ClusterInvoker;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.invoker.ConsumerContext;
import top.yunzhitan.rpc.model.RpcRequest;

public class ClusterInvokerFilter implements Filter {

    @Override
    public Type getType() {
        return Type.CONSUMER;
    }

    @Override
    public <T extends FilterContext> void doFilter(RpcRequest request, T filterCtx,FilterChain next) throws Throwable{
        ConsumerContext consumerContext = (ConsumerContext) filterCtx;
        ClusterInvoker invoker = consumerContext.getInvoker();
        Class<?> returnType = consumerContext.getReturnType();

        InvokeFuture<?> future = invoker.invoke(request,returnType);

        if(consumerContext.isSync()) {
            consumerContext.setResult(future.getResult());
        }
        else {
            consumerContext.setResult(future);
        }
    }
}
