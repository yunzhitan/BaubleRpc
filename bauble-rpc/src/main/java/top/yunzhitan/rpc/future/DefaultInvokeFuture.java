package top.yunzhitan.rpc.future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.Util.Signal;
import top.yunzhitan.common.Constants;
import top.yunzhitan.rpc.ConsumerHook;
import top.yunzhitan.rpc.Listener;
import top.yunzhitan.rpc.exception.BizException;
import top.yunzhitan.rpc.exception.RemoteException;
import top.yunzhitan.rpc.exception.SerializationException;
import top.yunzhitan.rpc.exception.TimeoutException;
import top.yunzhitan.rpc.model.RpcResponse;
import top.yunzhitan.transport.RemotePeer;
import top.yunzhitan.transport.Status;

import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

public class DefaultInvokeFuture<V> extends AbstractListenableFuture<V> implements InvokeFuture<V> {

    private static final Logger logger = LoggerFactory.getLogger(DefaultInvokeFuture.class);

    private static final long DEFAULT_TIMEOUT_NANOSECONDS = TimeUnit.MILLISECONDS.toNanos(Constants.DEFAULT_TIMEOUT);

    private final long invokeId; // request.invokeId, 广播的场景可以重复
    private final Class<V> returnType;
    private final long timeout;
    private final long startTime = System.nanoTime();
    private final RemotePeer remotePeer;

    private volatile boolean sent = false;

    private ConsumerHook[] hooks = ConsumerHook.EMPTY_HOOKS;

    public DefaultInvokeFuture(
            long invokeId, Class<V> returnType,RemotePeer remotePeer,
            long timeoutMillis) {

        this.invokeId = invokeId;
        this.remotePeer =remotePeer;
        this.returnType = returnType;
        this.timeout = timeoutMillis > 0 ? TimeUnit.MILLISECONDS.toNanos(timeoutMillis) : DEFAULT_TIMEOUT_NANOSECONDS;
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
            SocketAddress address = remotePeer.getRemoteAddress();
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
    public void doReceivedResponse(RpcResponse response) {
        Status status = response.getStatus();

        if (status == Status.OK) {
            set((V)response.getResult());
        } else {
            setException(status, response);
        }

        // call hook's after method
        for (int i = 0; i < hooks.length; i++) {
            hooks[i].after(response, remotePeer);
        }
    }

    private void setException(Status status, RpcResponse response) {
        Throwable cause;
        switch (status) {
            case SERVER_TIMEOUT:
                cause = new TimeoutException(remotePeer.getRemoteAddress(), Status.SERVER_TIMEOUT);
                break;
            case CLIENT_TIMEOUT:
                cause = new TimeoutException(remotePeer.getRemoteAddress(), Status.CLIENT_TIMEOUT);
                break;
            case DESERIALIZATION_FAIL:
                cause = (SerializationException) response.getResult();
                break;
            case SERVICE_EXPECTED_ERROR:
                cause = (Throwable) response.getResult();
                break;
            case SERVICE_UNEXPECTED_ERROR:
                String message = String.valueOf(response.getResult());
                cause = new BizException(message, remotePeer.getRemoteAddress());
                break;
            default:
                Object result = response.getResult();
                if (result != null && result instanceof RemoteException) {
                    cause = (RemoteException) result;
                } else {
                    cause = new RemoteException(response.toString(), remotePeer.getRemoteAddress());
                }
                break;
        }
        setException(cause);
    }

    public long getInvokeId() {
        return invokeId;
    }

    public long getTimeout() {
        return timeout;
    }

    public long getStartTime() {
        return startTime;
    }

    public RemotePeer getRemotePeer() {
        return remotePeer;
    }

    public boolean isSent() {
        return sent;
    }

    public ConsumerHook[] getHooks() {
        return hooks;
    }
}
