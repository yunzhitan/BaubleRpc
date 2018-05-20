package top.yunzhitan.rpc.future;

public interface TestFuture<V> extends ListenableFuture<V> {

    Class<V> getReturnType();

    /**
     * Waits for this future to be completed and get the result.
     */
    V getResult() throws Throwable;

    void setResult(V v);
}
