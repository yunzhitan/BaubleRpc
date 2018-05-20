package top.yunzhitan.rpc.future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.rpc.Listener;

public class FailOverFuture<V> extends AbstractListenableFuture<V> implements InvokeFuture<V> {

    private Class<V> returnType;
    private static final Logger logger = LoggerFactory.getLogger(FailOverFuture.class);

    public FailOverFuture(Class<V> returnType) {
        this.returnType = returnType;
    }

    public static <V> FailOverFuture<V> newFuture(Class<V> returnType) {
        return new FailOverFuture<>(returnType);
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
                    listener.getClass().getName(), state == COMPLETED ? "complete()" : "failure()",t);
        }
    }

    public void setSuccess() {

    }


    @Override
    public Class<V> getReturnType() {
        return returnType;
    }

    @Override
    public V getResult() throws Throwable {
        return get();
    }
}
