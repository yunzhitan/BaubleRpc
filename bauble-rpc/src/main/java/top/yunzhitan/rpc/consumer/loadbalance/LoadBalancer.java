package top.yunzhitan.rpc.consumer.loadbalance;

import top.yunzhitan.transport.RemotePeer;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 默认实现轮询的负载均衡机制 开放接口，可让用户自行定制
 */
public interface LoadBalancer {

    RemotePeer selectPeer(CopyOnWriteArrayList<RemotePeer> peerList);
}
