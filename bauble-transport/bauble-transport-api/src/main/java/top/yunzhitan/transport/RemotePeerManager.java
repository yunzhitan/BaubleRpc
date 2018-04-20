package top.yunzhitan.transport;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class RemotePeerManager {
    private final ConcurrentMap<String,CopyOnWriteArrayList<RemotePeer>> remotePeerMap
            = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<RemotePeer, AtomicInteger> peerRefCountMap
            = new ConcurrentHashMap<>();

    public CopyOnWriteArrayList<RemotePeer> find(Directory directory) {
        String _directory = directory.directory();
        return remotePeerMap.computeIfAbsent(_directory,k->new CopyOnWriteArrayList<RemotePeer>());
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
