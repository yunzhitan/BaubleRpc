package com.yunzhitan.client;

import com.yunzhitan.model.RpcRequest;
import com.yunzhitan.model.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;


@SuppressWarnings({"ALL", "NullableProblems"})
    public class RPCFuture implements Future<Object> {
        private static final Logger logger = LoggerFactory.getLogger(RPCFuture.class);

        private Sync sync;
        private RpcRequest request;
        private RpcResponse response;
        private boolean isAsync;
        private long startTime;

        private long responseTimeThreshold = 5000;

        private List<AsyncRPCCallback> pendingCallbacks = new ArrayList<>();
        private ReentrantLock lock = new ReentrantLock();

        public RPCFuture(RpcRequest request, Boolean isAsync) {
            this.sync = new Sync();
            this.request = request;
            this.isAsync = isAsync;
            this.startTime = System.currentTimeMillis();
        }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(-1);
        if (this.response != null) {
            return this.response.getResult();
        } else {
            return null;
        }
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Object get(long timeout, @SuppressWarnings("NullableProblems") TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            if (this.response != null) {
                return this.response.getResult();
            } else {
                return null;
            }
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + this.request.getRequestId()
                    + ". Request class name: " + this.request.getClassName()
                    + ". Request method: " + this.request.getMethodName());
        }
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    public void done(RpcResponse reponse) {
        this.response = reponse;
        sync.release(1); //release应该在done方法中完成
        if (isAsync)
            invokeCallbacks();
        // Threshold
        long responseTime = System.currentTimeMillis() - startTime;
        if (responseTime > this.responseTimeThreshold) {
            logger.warn("Service response time is too slow. Request id = " + reponse.getRequestId() + ". Response Time = " + responseTime + "ms");
        }
    }

    private void invokeCallbacks() {
        lock.lock();
        try {
            for (final AsyncRPCCallback callback : pendingCallbacks) {
                runCallback(callback);
            }
        } finally {
            lock.unlock();
        }
    }

    public RPCFuture addCallback(AsyncRPCCallback callback) {
        lock.lock();
        try {
            if (isDone()) {
                runCallback(callback);
            } else {
                this.pendingCallbacks.add(callback);
            }
        } finally {
            lock.unlock();
        }
        return this;
    }

    private void runCallback(final AsyncRPCCallback callback) {
        final RpcResponse res = this.response;
        RpcClient.submit(() -> {
            if (!res.isError()) {
                callback.success(res.getResult());
            } else {
                callback.fail(new RuntimeException("Response error", new Throwable(res.getError())));
            }
        });
    }

    static class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = 1L;

        //future status  == 1 rpcfuture完成 == 0 rpcfuture等到返回结果
        private static final int done = 1;
        private static final int pending = 0;

        /**
         * 查询一个rpcfuture是否完成
         *
         * @param acquires
         * @return
         */
        @Override
        protected boolean tryAcquire(int acquires) {
            return getState() == done;
        }

        /**
         * 将一个处于pedding状态的rpcfuture设置为done状态
         *
         * @param releases
         * @return
         */
        @Override
        protected boolean tryRelease(int releases) {
            if (getState() == pending) {
                if (compareAndSetState(pending, done)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isDone() {
            return getState() == done;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RPCFuture rpcFuture = (RPCFuture) o;

        if (isAsync != rpcFuture.isAsync) return false;
        return (request != null ? request.equals(rpcFuture.request) : rpcFuture.request == null) && (response != null ? response.equals(rpcFuture.response) : rpcFuture.response == null);
    }

    @Override
    public int hashCode() {
        int result = request != null ? request.hashCode() : 0;
        result = 31 * result + (response != null ? response.hashCode() : 0);
        result = 31 * result + (isAsync ? 1 : 0);
        return result;
    }
}
