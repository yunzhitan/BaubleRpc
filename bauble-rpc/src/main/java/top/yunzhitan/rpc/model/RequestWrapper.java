package top.yunzhitan.rpc.model;

import lombok.Data;
import top.yunzhitan.rpc.tracing.TraceId;

import java.util.Arrays;

@Data
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
