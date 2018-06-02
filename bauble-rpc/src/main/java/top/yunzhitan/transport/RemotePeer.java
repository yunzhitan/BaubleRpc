package top.yunzhitan.transport;

import lombok.Data;

import java.net.SocketAddress;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Data
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

    public boolean isAvailable() {
        return available.get();
    }


    public void addCallCount() {
        callCount.getAndIncrement();
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
