package top.yunzhitan.rpc.filter;

import top.yunzhitan.rpc.cluster.ClusterInvoker;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.invoker.InvokeContext;
import top.yunzhitan.rpc.model.RpcRequest;

public class ClusterInvokerFilter implements Filter {

    @Override
    public Type getType() {
        return Type.CONSUMER;
    }

    @Override
    public <T extends FilterContext> void doFilter(RpcRequest request, T filter) throws Throwable{
        InvokeContext invokeContext = (InvokeContext) filter;
        ClusterInvoker invoker = invokeContext.getInvoker();
        Class<?> returnType = invokeContext.getReturnType();

        InvokeFuture<?> future = invoker.invoke(request,returnType);

        if(invokeContext.isSync()) {
            invokeContext.setResult(future.getResult());
        }
        else {
            invokeContext.setResult(future);
        }
    }
}
