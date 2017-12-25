package com.yunzhitan.server.threadpool;

import java.util.concurrent.*;

public class ProviderExecutor {
    public static Executor getExecutor(int threads, int queues) {
        System.out.println("ThreadPool Core[threads:" + threads + ", queues:" + queues + "]");
        String name = "RpcThreadPool";
        ThreadPoolExecutor executor = new ThreadPoolExecutor(threads, threads, 0,
                TimeUnit.MILLISECONDS,
                createBlockingQueue(queues),
                new NamedThreadFactory(name, true));
        return executor;
    }

    private static BlockingQueue<Runnable> createBlockingQueue(int queues) {
        BlockingQueueType queueType = BlockingQueueType.fromString("LinkedBlockingQueue");

        switch (queueType) {
            case LINKED_BLOCKING_QUEUE:
                return new LinkedBlockingQueue<>();
            case ARRAY_BLOCKING_QUEUE:
                return new ArrayBlockingQueue<>(2 * queues);
            case SYNCHRONOUS_QUEUE:
                return new SynchronousQueue<>();
            default: {
                break;
            }
        }

        return null;
    }

}
