package top.yunzhitan.rpc.invoker;

import top.yunzhitan.rpc.cluster.ClusterInvoker;
import top.yunzhitan.rpc.cluster.ClusterUtil;
import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.rpc.model.Service;
import top.yunzhitan.rpc.filter.*;
import top.yunzhitan.rpc.model.*;
import top.yunzhitan.rpc.tracing.TraceId;
import top.yunzhitan.rpc.tracing.TracingUtil;

import java.util.List;

public abstract class AbstructInvoker {


    private final String appName;            //应用名称
    private final Service service;   //服务元数据
    private final ClusterUtil clusterUtil;

    public AbstructInvoker(String appName,
                           Service metadata,
                           Transporter transporter,
                           ClusterTypeConfig defaultStrategy,
                           List<MethodSpecialConfig> methodSpecialConfigs) {
        this.appName = appName;
        this.service = metadata;
        clusterUtil = new ClusterUtil(transporter, defaultStrategy, methodSpecialConfigs);
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
        FilterHandler.filter(request,invokeContext);
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

    private static class FilterHandler {

        private static final FilterChain headFilter;

        static {
            FilterChain invokeHandler = new DefaultFilterChain(new ClusterInvokerFilter(), null);
            headFilter = FilterLoader.loadExtFilters(invokeHandler, Filter.Type.CONSUMER);
        }

        public static <T extends FilterContext> T filter(RpcRequest request, T invokeCtx) throws Throwable {
            headFilter.doFilter(request, invokeCtx);
            return invokeCtx;
        }
    }

}

