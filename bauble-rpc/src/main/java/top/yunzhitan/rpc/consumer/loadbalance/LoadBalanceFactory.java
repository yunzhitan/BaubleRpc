package top.yunzhitan.rpc.consumer.loadbalance;

import top.yunzhitan.Util.BaubleServiceLoader;
import top.yunzhitan.rpc.model.ConsumerConfig;

public final class LoadBalanceFactory {

    public static LoadBalancer getLoadBalancer(ConsumerConfig consumerConfig) {
        String loadBalancerType = consumerConfig.getLoadBalancer();
        LoadBalancer loadBalancer = BaubleServiceLoader.load(LoadBalancer.class).find(loadBalancerType);

        if (loadBalancer != null) {
            return loadBalancer;
        } else {
            return RandomLoadBalancer.getInstance();
        }
    }

}

