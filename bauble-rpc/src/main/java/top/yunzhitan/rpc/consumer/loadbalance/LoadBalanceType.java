package top.yunzhitan.rpc.consumer.loadbalance;


public enum LoadBalanceType {
    RANDOM,
    ROUND_ROBIN;


    public static LoadBalanceType parse(String data) {
        for(LoadBalanceType type : values()) {
            if(type.name().equalsIgnoreCase(data)) {
                return type;
            }
        }
        return null;
    }

    public static LoadBalanceType getDefault() {
        return RANDOM;
    }

}
