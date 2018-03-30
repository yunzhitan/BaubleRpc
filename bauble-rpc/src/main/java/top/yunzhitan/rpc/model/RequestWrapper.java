package top.yunzhitan.rpc.model;

import top.yunzhitan.rpc.tracing.TraceId;

import java.util.Arrays;

public class RequestWrapper {

    private String appName;

    private String methodName;

    private Object[] arguments;

    private TraceId traceId;

    public RequestWrapper(String appName, String methodName, Object[] arguments) {
        this.appName = appName;
        this.methodName = methodName;
        this.arguments = arguments;
    }

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
        return "RequestWrapper{" +
                "getAppname='" + appName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", arguments=" + Arrays.toString(arguments) +
                ", traceId=" + traceId +
                '}';
    }
}
