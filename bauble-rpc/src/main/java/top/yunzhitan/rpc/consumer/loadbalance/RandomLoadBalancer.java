package top.yunzhitan.rpc.consumer.loadbalance;

import top.yunzhitan.transport.RemotePeer;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 加权轮询 负载均衡算法
 */
public class RandomLoadBalancer implements LoadBalancer{

    private static final RandomLoadBalancer instance = new RandomLoadBalancer();
    private final Random random = new Random();

    public static RandomLoadBalancer getInstance() {
        return instance;
    }

    private RandomLoadBalancer() {
    }

    @Override
    public RemotePeer select(CopyOnWriteArrayList<RemotePeer> remotePeers) {
        int length = remotePeers.size();
        int totalWeight = 0;
        int weight = 0, lastWeight = 0;
        boolean allTheSameWeight = true;
        for(int i = 0; i < length; ++i) {
            lastWeight = weight;
            weight = remotePeers.get(i).getWeight();
            totalWeight += weight;
            if(allTheSameWeight && i > 0 && lastWeight != weight) {
                allTheSameWeight = false;
            }
        }

        if(totalWeight > 0 && !allTheSameWeight) {
            int offset = random.nextInt(totalWeight);
            for(RemotePeer remotePeer : remotePeers) {
                offset -= remotePeer.getWeight();
                if (offset < 0) {
                    return remotePeer;
                }
            }

        }

        return remotePeers.get(random.nextInt(length));
    }

}
