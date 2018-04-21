package top.yunzhitan.rpc.consumer.loadbalance;

public final class LoadBalanceFactory {

    public static LoadBalancer loadBalancer(LoadBalanceType type) {
        if (type == LoadBalanceType.RANDOM) {
            return RandomLoadBalancer.getInstance();
        }

        if (type == LoadBalanceType.ROUND_ROBIN) {
            return RoundRubinBalancer.getInstance();
        }

        // 如果不指定, 默认的负载均衡算法是加权轮询
        return RandomLoadBalancer.getInstance();
    }

    private LoadBalanceFactory() {}
}

