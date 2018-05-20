package top.yunzhitan.transport;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RemotePeer {

    /**
     * 远程服务节点的地址
     */
    private SocketAddress remoteAddress;
    /**
     * 远程服务节点的权重
     */
    private int weight;

    /**
     * 调用次数
     */
    private AtomicInteger callCount = new AtomicInteger(0);

    private AtomicBoolean available = new AtomicBoolean(true);

    public RemotePeer(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public RemotePeer(SocketAddress remoteAddress, int weight) {
        this.remoteAddress = remoteAddress;
        this.weight = weight;
    }

    public void setAvailable(boolean aval) {
        available.getAndSet(aval);
    }

    public boolean isAvailable() {
        return available.get();
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public void addCallCount() {
        callCount.getAndIncrement();
    }

    public AtomicInteger getCallCount() {
        return callCount;
    }

    @Override
    public String toString() {
        return "RemotePeer{" +
                "remoteAddress=" + remoteAddress +
                ", weight=" + weight +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RemotePeer)) return false;
        RemotePeer that = (RemotePeer) o;
        return Objects.equals(getRemoteAddress(), that.getRemoteAddress());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getRemoteAddress());
    }
}
