package top.yunzhitan.transport;

import top.yunzhitan.Util.ThrowUtil;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class RemotePeer {

    /**
     * 远程服务节点的地址
     */
    private SocketAddress remoteAddress;
    /**
     * 远程服务节点的权重
     */
    private int weight;
    private AtomicBoolean available = new AtomicBoolean(false);
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notifyCondition = lock.newCondition();
    private volatile int signalNeeded = 0; // 0: false, 1: true

    public RemotePeer(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
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

    public boolean waitForAvailable(long timeoutMillis) {
        boolean available = isAvailable();
        if (available) {
            return true;
        }
        long remains = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);

        final ReentrantLock _look = lock;
        _look.lock();
        try {
            // avoid "spurious wakeup" occurs
            while (!(available = isAvailable())) {
                signalNeeded = 1; // set signal needed to true
                if ((remains = notifyCondition.awaitNanos(remains)) <= 0) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            ThrowUtil.throwException(e);
        } finally {
            _look.unlock();
        }

        return available;
    }

    @Override
    public String toString() {
        return "RemotePeer{" +
                "remoteAddress=" + remoteAddress +
                '}';
    }
}
