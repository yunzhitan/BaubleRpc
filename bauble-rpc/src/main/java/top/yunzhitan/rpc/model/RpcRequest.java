package top.yunzhitan.rpc.model;

import top.yunzhitan.transport.RequestMessage;
import top.yunzhitan.rpc.tracing.TraceId;

import java.io.Serializable;
import java.util.Arrays;

public class RpcRequest implements Serializable{

    private static final long serialVersionUID = -47365345647583434L;
    private String appName;

    private String methodName;

    private Object[] arguments;

    private TraceId traceId;


    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Object[] getArguments() {
        return arguments;
    }

    public void setArguments(Object[] arguments) {
        this.arguments = arguments;
    }

    public TraceId getTraceId() {
        return traceId;
    }

    public void setTraceId(TraceId traceId) {
        this.traceId = traceId;
    }

    @Override
    public String toString() {
        return "RpcRequest{" +
                "appName='" + appName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
    }
}