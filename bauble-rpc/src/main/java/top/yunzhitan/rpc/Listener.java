package top.yunzhitan.rpc;

import java.util.EventListener;

/**
 * Callback is often triggered by the core thread (may be an IO thread).
 * Be careful, do not to have time-consuming operations within
 * {@link #complete(Object)} and {@link #failure(Throwable)}.
 *
 */
public interface Listener<V> extends EventListener {

    /**
     * Returns result when the call succeeds.
     */
    void complete(V result);

    /**
     * Returns an exception message when call fails.
     */
    void failure(Throwable cause);
}

