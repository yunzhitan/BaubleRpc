package top.yunzhitan.rpc.invoker;

import top.yunzhitan.rpc.future.InvokeFuture;

public class InvokeFutureContext {
    private static final ThreadLocal<InvokeFuture<?>> futureThreadLocal = new ThreadLocal<>();

    /**
     * 获取InvokeFuture
     * @return
     */
    public static InvokeFuture<?> getFuture() {
        InvokeFuture<?> future = futureThreadLocal.get();
        futureThreadLocal.remove();
        return future;
    }

    public static void setFuture(InvokeFuture<?> future) {
        futureThreadLocal.set(future);
    }
}
