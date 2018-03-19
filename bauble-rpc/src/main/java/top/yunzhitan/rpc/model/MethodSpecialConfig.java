package top.yunzhitan.rpc.model;

import java.io.Serializable;

public class MethodSpecialConfig implements Serializable {

    private static final long serialVersionUID = -3689442191636868738L;

    private final String methodName;

    private long timeoutMillis;
    private ClusterTypeConfig typeConfig;

    public static MethodSpecialConfig of(String methodName) {
        return new MethodSpecialConfig(methodName);
    }

    private MethodSpecialConfig(String methodName) {
        this.methodName = methodName;
    }

    public MethodSpecialConfig timeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
        return this;
    }

    public MethodSpecialConfig strategy(ClusterTypeConfig strategy) {
        this.typeConfig = strategy;
        return this;
    }

    public String getMethodName() {
        return methodName;
    }

    public long getTimeoutMillis() {
        return timeoutMillis;
    }

    public void setTimeoutMillis(long timeoutMillis) {
        this.timeoutMillis = timeoutMillis;
    }

    public ClusterTypeConfig getStrategy() {
        return typeConfig;
    }

    public void setStrategy(ClusterTypeConfig strategy) {
        this.typeConfig = strategy;
    }
}

