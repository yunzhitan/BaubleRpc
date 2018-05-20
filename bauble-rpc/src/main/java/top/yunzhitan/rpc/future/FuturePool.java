package top.yunzhitan.rpc.future;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.rpc.model.RpcResponse;
import top.yunzhitan.transport.RemotePeer;
import top.yunzhitan.transport.Status;

import java.util.concurrent.ConcurrentMap;

public class FuturePool {

    private static final Logger logger = LoggerFactory.getLogger(FuturePool.class);

    private static final ConcurrentMap<Long, DefaultInvokeFuture<?>> savedFuture = Maps.newConcurrentMap();

    public static <T> DefaultInvokeFuture<T> newDefaultFuture(
            long invokeId, Class<T> returnType,RemotePeer remotePeer,
            long timeoutMillis) {
        DefaultInvokeFuture<T> future =  new DefaultInvokeFuture<>(invokeId, returnType, remotePeer,timeoutMillis);
        savedFuture.put(invokeId, future);

        return future;
    }

    public static void receiveResponse(RemotePeer remotePeer, RpcResponse response) {
        long invokeId = response.getInvokeId();
        DefaultInvokeFuture<?> future = savedFuture.remove(invokeId);
        if (future == null) {
            logger.warn("A timeout response [{}] finally returned on {}.", response, remotePeer);
            return;
        }

        future.doReceivedResponse(response);
    }


    // timeout scanner
    @SuppressWarnings("all")
    private static class TimeoutScanner implements Runnable {

        public void run() {
            for (;;) {
                try {
                    for (DefaultInvokeFuture<?> future : savedFuture.values()) {
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

            if (System.nanoTime() - future.getStartTime() > future.getTimeout()) {
                RpcResponse response = new RpcResponse(future.getInvokeId());
                response.setStatus(future.isSent() ? Status.SERVER_TIMEOUT : Status.CLIENT_TIMEOUT);

                FuturePool.receiveResponse(future.getRemotePeer(), response);
            }
        }
    }

    static {
        Thread t = new Thread(new TimeoutScanner(), "timeout.scanner");
        t.setDaemon(true);
        t.start();
    }


}
