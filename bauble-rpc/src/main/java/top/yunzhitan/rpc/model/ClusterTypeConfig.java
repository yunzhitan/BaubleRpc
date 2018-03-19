package top.yunzhitan.rpc.model;

import top.yunzhitan.rpc.cluster.ClusterType;

import java.io.Serializable;

public class ClusterTypeConfig implements Serializable {

    private static final long serialVersionUID = 8192956131353063709L;

    private ClusterType clusterType;
    private int failoverRetries;

    public static ClusterTypeConfig of(String strategy, String failoverRetries) {
        int retries = 0;
        try {
            retries = Integer.parseInt(failoverRetries);
        } catch (Exception ignored) {}

        return of(ClusterType.parse(strategy), retries);
    }

    public static ClusterTypeConfig of(ClusterType type, int failoverRetries) {
        ClusterTypeConfig s = new ClusterTypeConfig();
        s.setClusterType(type);
        s.setFailoverRetries(failoverRetries);
        return s;
    }

    public ClusterType getClusterType() {
        return clusterType;
    }

    public void setClusterType(ClusterType clusterType) {
        this.clusterType = clusterType;
    }

    public int getFailoverRetries() {
        return failoverRetries;
    }

    public void setFailoverRetries(int failoverRetries) {
        this.failoverRetries = failoverRetries;
    }
}

