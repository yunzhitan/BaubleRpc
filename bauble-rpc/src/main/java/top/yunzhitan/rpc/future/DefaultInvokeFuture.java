package top.yunzhitan.rpc.future;

import com.google.common.collect.Maps;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.Util.Signal;
import top.yunzhitan.common.Constants;
import top.yunzhitan.rpc.ConsumerHook;
import top.yunzhitan.rpc.DispatchType;
import top.yunzhitan.rpc.Listener;
import top.yunzhitan.rpc.exception.BizException;
import top.yunzhitan.rpc.exception.RemoteException;
import top.yunzhitan.rpc.exception.SerializationException;
import top.yunzhitan.rpc.exception.TimeoutException;
import top.yunzhitan.rpc.model.RpcResponse;
import top.yunzhitan.transport.Status;

import java.net.SocketAddress;
import java.util.Vector;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

public class DefaultInvokeFuture<V> extends AbstructListenableFuture<V> implements InvokeFuture<V> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultInvokeFuture.class);

    private static final long DEFAULT_TIMEOUT_NANOSECONDS = TimeUnit.MILLISECONDS.toNanos(Constants.DEFAULT_TIMEOUT);

    private static final ConcurrentMap<Long, DefaultInvokeFuture<?>> roundFutures = Maps.newConcurrentMap();
    private static final ConcurrentMap<String, DefaultInvokeFuture<?>> broadcastFutures = Maps.newConcurrentMap();

    private final long invokeId; // request.invokeId, 广播的场景可以重复
    private final Channel channel;
    private final Class<V> returnType;
    private final long timeout;
    private final long startTime = System.nanoTime();

    private volatile boolean sent = false;

    private ConsumerHook[] hooks = ConsumerHook.EMPTY_HOOKS;
    private CopyOnWriteArrayList


    public static <T> DefaultInvokeFuture<T> with(
            long invokeId, Channel channel, Class<T> returnType, long timeoutMillis, DispatchType dispatchType) {
        Vector vector = new Vector();
        vector.get()
        return new DefaultInvokeFuture<>(invokeId, channel, returnType, timeoutMillis, dispatchType);
    }

    private DefaultInvokeFuture(
            long invokeId, Channel channel, Class<V> returnType, long timeoutMillis, DispatchType dispatchType) {

        this.invokeId = invokeId;
        this.channel = channel;
        this.returnType = returnType;
        this.timeout = timeoutMillis > 0 ? TimeUnit.MILLISECONDS.toNanos(timeoutMillis) : DEFAULT_TIMEOUT_NANOSECONDS;

        switch (dispatchType) {
            case ROUND:
                roundFutures.put(invokeId, this);
                break;
            case BROADCAST:
                broadcastFutures.put(subInvokeId(channel, invokeId), this);
                break;
            default:
                throw new IllegalArgumentException("unsupported " + dispatchType);
        }
    }

    @Override
    public Class<V> getReturnType() {
        return returnType;
    }

    @Override
    public V getResult() throws Throwable {
        try {
            return get(timeout, TimeUnit.NANOSECONDS);
        } catch (Signal s) {
            SocketAddress address = channel.remoteAddress();
            if (s == TIMEOUT) {
                throw new TimeoutException(address, sent ? Status.SERVER_TIMEOUT : Status.CLIENT_TIMEOUT);
            } else {
                throw new RemoteException(s.name(), address);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void notifyListener0(Listener<V> listener, int state, Object x) {
        try {
            if (state == COMPLETED) {
                listener.complete((V) x);
            } else {
                listener.failure((Throwable) x);
            }
        } catch (Throwable t) {
            logger.error("An exception was thrown by {}.{}, {}.",
                    listener.getClass().getName(), state == COMPLETED ? "complete()" : "failure()", t);
        }
    }

    public void markSent() {
        sent = true;
    }

    public ConsumerHook[] hooks() {
        return hooks;
    }

    public DefaultInvokeFuture<V> hooks(ConsumerHook[] hooks) {

        this.hooks = hooks;
        return this;
    }

    @SuppressWarnings("all")
    private void doReceivedResponse(RpcResponse response) {
        Status status = response.getStatus();

        if (status == Status.OK) {
            set((V)response.getResult());
        } else {
            setException(status, response);
        }

        // call hook's after method
        for (int i = 0; i < hooks.length; i++) {
            hooks[i].after(response, channel);
        }
    }

    private void setException(Status status, RpcResponse response) {
        Throwable cause;
        if (status == Status.SERVER_TIMEOUT) {
            cause = new TimeoutException(channel.remoteAddress(), Status.SERVER_TIMEOUT);
        } else if (status == Status.CLIENT_TIMEOUT) {
            cause = new TimeoutException(channel.remoteAddress(), Status.CLIENT_TIMEOUT);
        } else if (status == Status.DESERIALIZATION_FAIL) {
            cause = (SerializationException) response.getResult();
        } else if (status == Status.SERVICE_EXPECTED_ERROR) {
            cause = (Throwable) response.getResult();
        } else if (status == Status.SERVICE_UNEXPECTED_ERROR) {
            String message = String.valueOf(response.getResult());
            cause = new BizException(message, channel.remoteAddress());
        } else {
            Object result = response.getResult();
            if (result != null && result instanceof RemoteException) {
                cause = (RemoteException) result;
            } else {
                cause = new RemoteException(response.toString(), channel.remoteAddress());
            }
        }
        setException(cause);
    }

    public static void receiveResponse(Channel channel, RpcResponse response) {
        long invokeId = response.getInvokeId();
        DefaultInvokeFuture<?> future = roundFutures.remove(invokeId);
        if (future == null) {
            // 广播场景下做出了一点让步, 多查询了一次roundFutures
            future = broadcastFutures.remove(subInvokeId(channel, invokeId));
        }
        if (future == null) {
            logger.warn("A timeout response [{}] finally returned on {}.", response, channel);
            return;
        }

        future.doReceivedResponse(response);
    }

    private static String subInvokeId(Channel channel, long invokeId) {
        return channel.id().toString() + invokeId;
    }

    // timeout scanner
    @SuppressWarnings("all")
    private static class TimeoutScanner implements Runnable {

        public void run() {
            for (;;) {
                try {
                    // round
                    for (DefaultInvokeFuture<?> future : roundFutures.values()) {
                        process(future);
                    }

                    // broadcast
                    for (DefaultInvokeFuture<?> future : broadcastFutures.values()) {
                        process(future);
                    }
                } catch (Throwable t) {
                    logger.error("An exception was caught while scanning the timeout futures {}.", t);
                }

                try {
                    Thread.sleep(30);
                } catch (InterruptedException ignored) {}
            }
        }

        private void process(DefaultInvokeFuture<?> future) {
            if (future == null || future.isDone()) {
                return;
            }

            if (System.nanoTime() - future.startTime > future.timeout) {
                RpcResponse response = new RpcResponse(future.invokeId);
                response.setStatus(future.sent ? Status.SERVER_TIMEOUT : Status.CLIENT_TIMEOUT);

                DefaultInvokeFuture.receiveResponse(future.channel, response);
            }
        }
    }

    static {
        Thread t = new Thread(new TimeoutScanner(), "timeout.scanner");
        t.setDaemon(true);
        t.start();
    }

}
