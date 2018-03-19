package top.yunzhitan.rpc.invoker;

import top.yunzhitan.rpc.cluster.ClusterInvoker;
import top.yunzhitan.rpc.cluster.ClusterUtil;
import top.yunzhitan.rpc.model.ServiceMeta;
import top.yunzhitan.rpc.consumer.Dispatcher;
import top.yunzhitan.rpc.filter.*;
import top.yunzhitan.rpc.model.*;
import top.yunzhitan.rpc.tracing.TraceId;
import top.yunzhitan.rpc.tracing.TracingUtil;
import top.yunzhitan.transport.TraceUtil;

import java.util.List;

public abstract class AbstructInvoker {


    private final String appName;            //应用名称
    private final ServiceMeta serviceMeta;   //服务元数据
    private final ClusterUtil clusterUtil;

    public AbstructInvoker(String appName,
                           ServiceMeta metadata,
                           Dispatcher dispatcher,
                           ClusterTypeConfig defaultStrategy,
                           List<MethodSpecialConfig> methodSpecialConfigs) {
        this.appName = appName;
        this.serviceMeta = metadata;
        clusterUtil = new ClusterUtil(dispatcher, defaultStrategy, methodSpecialConfigs);
    }




    private RpcRequest createRequest(String methodName, Object[] args) {
        RpcRequest request = new RpcRequest();
        RequestWrapper wrapper = new RequestWrapper(appName,methodName,args);
        setTraceId(wrapper);
        request.setRequestWrapper(wrapper);

        return request;
    }

    protected Object doInvoke(String methodName,Object[] args,Class<?> returnType,boolean sync) throws Throwable {
        RpcRequest request = createRequest(methodName,args);
        ClusterInvoker invoker = clusterUtil.findClusterInvoker(methodName);

        InvokeContext invokeContext = new InvokeContext(invoker,returnType,sync);
        Chains.invoke(request,invokeContext);
        return invokeContext.getResult();
    }

    private void setTraceId(RequestWrapper message) {
        if (TracingUtil.isTracingNeeded()) {
            TraceId traceId = TracingUtil.getCurrent();
            if (traceId == TraceId.NULL_TRACE_ID) {
                traceId = TraceId.newInstance(TracingUtil.generateTraceId());
            }
            message.setTraceId(traceId);
        }
    }

    static class Chains {

        private static final FilterChain headChain;

        static {
            FilterChain invokeChain = new DefaultFilterChain(new ClusterInvokerFilter(), null);
            headChain = FilterLoader.loadExtFilters(invokeChain, Filter.Type.CONSUMER);
        }

        public static <T extends FilterContext> T invoke(RpcRequest request, T invokeCtx) throws Throwable {
            headChain.doFilter(request, invokeCtx);
            return invokeCtx;
        }
    }

}

