package top.yunzhitan.rpc.consumer.loadbalance;

public final class LoadBalanceFactory {

    public static LoadBalancer loadBalancer(LoadBalanceType type) {
        if (type == LoadBalanceType.RANDOM) {
            return RandomLoadBalancer.getInstance();
        }

        if (type == LoadBalanceType.ROUND_ROBIN) {
            return RoundRubinBalancer.getInstance();
        }

        return RoundRubinBalancer.getInstance();
        }

    private LoadBalanceFactory() {}
}

