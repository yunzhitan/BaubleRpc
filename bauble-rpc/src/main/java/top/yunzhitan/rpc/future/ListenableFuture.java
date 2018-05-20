package top.yunzhitan.rpc.future;

import top.yunzhitan.rpc.Listener;

/**
 * A future that accepts completion listeners.
 *
 */
@SuppressWarnings("unchecked")
public interface ListenableFuture<V> {

    /**
     * Adds the specified listener to this future.  The
     * specified listener is notified when this future is
     * done.  If this future is already completed, the
     * specified listener is notified immediately.
     */
    ListenableFuture<V> addListener(Listener<V> listener);

    /**
     * Adds the specified listeners to this future.  The
     * specified listeners are notified when this future is
     * done.  If this future is already completed, the
     * specified listeners are notified immediately.
     */
    ListenableFuture<V> addListeners(Listener<V>... listeners);

    /**
     * Removes the first occurrence of the specified listener from this future.
     * The specified listener is no longer notified when this
     * future is done.  If the specified listener is not associated
     * newDefaultFuture this future, this method does nothing and returns silently.
     */
    ListenableFuture<V> removeListener(Listener<V> listener);

    /**
     * Removes the first occurrence for each of the listeners from this future.
     * The specified listeners are no longer notified when this
     * future is done.  If the specified listeners are not associated
     * newDefaultFuture this future, this method does nothing and returns silently.
     */
    ListenableFuture<V> removeListeners(Listener<V>... listeners);
}
