package top.yunzhitan.rpc.consumer.loadbalance;

import top.yunzhitan.transport.RemotePeer;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 随机负载均衡 有状态，不能单例模式
 */
public class RoundRubinBalancer implements LoadBalancer{

    public static RoundRubinBalancer getInstance() {
        return new RoundRubinBalancer();
    }

    private RoundRubinBalancer() {
    }

    @Override
    public RemotePeer select(CopyOnWriteArrayList<RemotePeer> peerList) {
        return null;
    }
}
