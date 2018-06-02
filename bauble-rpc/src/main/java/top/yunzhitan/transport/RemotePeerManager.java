package top.yunzhitan.transport;

import top.yunzhitan.registry.ProviderConfig;
import top.yunzhitan.common.ServiceConfig;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class RemotePeerManager {
    private final ConcurrentMap<String,CopyOnWriteArrayList<RemotePeer>> remotePeerMap
            = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<RemotePeer, AtomicInteger> peerRefCountMap
            = new ConcurrentHashMap<>();
    private final ConcurrentMap<ProviderConfig,RemotePeer> remotePeerCache = new ConcurrentHashMap<>();

    public CopyOnWriteArrayList<RemotePeer> find(ServiceConfig serviceConfig) {
        String _directory = serviceConfig.getDirectory();
        return remotePeerMap.computeIfAbsent(_directory,k-> new CopyOnWriteArrayList<>());
    }

    public CopyOnWriteArrayList<RemotePeer> find(String directory) {
        return remotePeerMap.computeIfAbsent(directory,k-> new CopyOnWriteArrayList<>());
    }

    public RemotePeer findRemotePeer(ProviderConfig providerConfig) {
        RemotePeer remotePeer = remotePeerCache.get(providerConfig);
        if(remotePeer == null) {
            SocketAddress address = new InetSocketAddress(providerConfig.getHost(), providerConfig.getPort());
            int weight = providerConfig.getWeight();
            remotePeer = new RemotePeer(address,weight);
            remotePeerCache.put(providerConfig,remotePeer);
        }
        return remotePeer;
    }





    public int incrementRefCount(RemotePeer peer) {
        return getRefCount(peer).incrementAndGet();
    }

    public int decrementRefCount(RemotePeer peer) {
        AtomicInteger counter = getRefCount(peer);
        if (counter.get() == 0) {
            return 0;
        }
        int count = counter.decrementAndGet();
        if (count == 0) {
            peerRefCountMap.remove(peer);
        }
        return count;
    }


    public AtomicInteger getRefCount(RemotePeer remotePeer) {
        return peerRefCountMap.computeIfAbsent(remotePeer,K->new AtomicInteger(0));
    }
}
