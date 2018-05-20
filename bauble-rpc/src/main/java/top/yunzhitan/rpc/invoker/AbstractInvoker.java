package top.yunzhitan.rpc.invoker;

import top.yunzhitan.rpc.cluster.ClusterInvoker;
import top.yunzhitan.rpc.cluster.ClusterUtil;
import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.common.Service;
import top.yunzhitan.rpc.filter.*;
import top.yunzhitan.rpc.model.*;
import top.yunzhitan.rpc.tracing.TraceId;
import top.yunzhitan.rpc.tracing.TracingUtil;

import java.util.List;

public abstract class AbstractInvoker {


    private final String appName;            //应用名称
    private final Service service;   //服务元数据
    private final ClusterUtil clusterUtil;

    public AbstractInvoker(String appName,
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
        request.setAppName(appName);
        request.setMethodName(methodName);
        request.setArguments(args);
        request.setService(service);

        return request;
    }

    protected Object doInvoke(String methodName,Object[] args,Class<?> returnType,boolean sync) throws Throwable {
        RpcRequest request = createRequest(methodName,args);
        ClusterInvoker invoker = clusterUtil.findClusterInvoker(methodName);

        ConsumerContext consumerContext = new ConsumerContext(invoker,returnType,sync);
        ClientFilterHandler.filter(request, consumerContext);
        return consumerContext.getResult();
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


}

