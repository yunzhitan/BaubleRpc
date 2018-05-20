package top.yunzhitan.rpc.invoker;

import top.yunzhitan.rpc.cluster.ClusterInvoker;
import top.yunzhitan.rpc.filter.Filter;
import top.yunzhitan.rpc.filter.FilterContext;

public class ConsumerContext implements FilterContext {
    private final ClusterInvoker invoker;
    private final Class<?> returnType;
    private final boolean sync;

    private Object result;

    public ConsumerContext(ClusterInvoker invoker, Class<?> returnType, boolean sync) {
        this.invoker = invoker;
        this.returnType = returnType;
        this.sync = sync;
    }

    @Override
    public Filter.Type getType() {
        return Filter.Type.CONSUMER;
    }

    public ClusterInvoker getInvoker() {
        return invoker;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    public boolean isSync() {
        return sync;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

}
