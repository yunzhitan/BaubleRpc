package top.yunzhitan.rpc.cluster;

import top.yunzhitan.Util.BaubleServiceLoader;
import top.yunzhitan.registry.ConsumerBoy;
import top.yunzhitan.rpc.model.ConsumerConfig;

public class ClusterFactory {

    public static Cluster getCluster(ConsumerConfig consumerConfig, ConsumerBoy consumerBoy) {
        Cluster cluster = BaubleServiceLoader.load(Cluster.class).find(consumerConfig.getClusterType());

        if (cluster == null) {
            throw new RuntimeException("Unsupported clusterType of consumerBoy!");
        }

        cluster.setConsumerConfig(consumerConfig);
        cluster.setConsumerBoy(consumerBoy);
        return cluster;
    }
}
