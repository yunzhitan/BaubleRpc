package top.yunzhitan.rpc.cluster;

public enum ClusterType {
    FAIL_FAST,      //快速失败
    FAIL_OVER,      //快速重试
    FAIL_SAFE;      //失败安全

    public static ClusterType parse(String name) {
        for (ClusterType type : values()) {
            if(type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public static ClusterType getDefault() {
        return FAIL_FAST;
    }
}
