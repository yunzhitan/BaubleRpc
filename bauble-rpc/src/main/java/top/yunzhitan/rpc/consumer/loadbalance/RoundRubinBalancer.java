package top.yunzhitan.rpc.consumer.loadbalance;

import top.yunzhitan.Util.IntSequence;
import top.yunzhitan.transport.RemotePeer;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 随机负载均衡 有状态，不能单例模式
 */
public class RoundRubinBalancer implements LoadBalancer{

    public static RoundRubinBalancer getInstance() {
        return new RoundRubinBalancer();
    }

    private static IntSequence sequence;

    private RoundRubinBalancer() {
    }

    @Override
    public RemotePeer select(CopyOnWriteArrayList<RemotePeer> peerList) {
        RemotePeer[] remotePeers = new RemotePeer[peerList.size()];
        peerList.toArray(remotePeers);
        int length = remotePeers.length;

        if (length == 0) {
            return null;
        }

        if (length == 1) {
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

        int index = sequence.next() & Integer.MAX_VALUE;

        if (allTheSameWeight) {
            return remotePeers[index % length];
        }

        int eVal = index % totalWeight;

        return remotePeers[eVal];

    }
}
