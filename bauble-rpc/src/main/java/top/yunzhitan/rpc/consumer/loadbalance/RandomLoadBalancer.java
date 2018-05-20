package top.yunzhitan.rpc.consumer.loadbalance;

import top.yunzhitan.transport.RemotePeer;

import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 加权轮询 负载均衡算法
 */
public class RandomLoadBalancer implements LoadBalancer{

    private static final RandomLoadBalancer instance = new RandomLoadBalancer();
    private final ThreadLocalRandom random = ThreadLocalRandom.current();

    public static RandomLoadBalancer getInstance() {
        return instance;
    }

    private RandomLoadBalancer() {
    }

    @Override
    public RemotePeer select(CopyOnWriteArrayList<RemotePeer> peerList) {
        RemotePeer[] remotePeers = new RemotePeer[peerList.size()];
        peerList.toArray(remotePeers);

        int length = remotePeers.length;

        if(length == 0) {
            return null;
        }
        if(length == 1) {
            return remotePeers[0];
        }

        int totalWeight = 0;
        int weight = 0, lastWeight = 0;
        boolean allTheSameWeight = true;
        for(int i = 0; i < length; ++i) {
            lastWeight = weight;
            weight = remotePeers[i].getWeight();
            totalWeight += weight;
            if(allTheSameWeight && i > 0 && lastWeight != weight) {
                allTheSameWeight = false;
            }
        }

        if(totalWeight > 0 && !allTheSameWeight) {
            int offset = random.nextInt(totalWeight);
            for(RemotePeer remotePeer : peerList) {
                offset -= remotePeer.getWeight();
                if (offset < 0) {
                    return remotePeer;
                }
            }

        }
        int index = random.nextInt(length);
        return remotePeers[index];
    }

}
