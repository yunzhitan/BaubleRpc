package top.yunzhitan.registry;


import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.Util.collection.ConcurrentSet;
import top.yunzhitan.common.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractRegistryService implements RegistryService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRegistryService.class);

    private final LinkedBlockingQueue<RegistryConfig> queue = new LinkedBlockingQueue<>();
    /**
     * 用于接受注册信息的线程
     */
    private final ExecutorService registerExecutor = Executors.newSingleThreadExecutor();

    /**
     * 用于注册失败后的处理线程
     */
    private final ScheduledExecutorService scheduleExecutor = Executors.newSingleThreadScheduledExecutor();

    private AtomicBoolean shutdown = new AtomicBoolean(false);

    /**
     * provider注册的服务信息
     */
    private final ConcurrentSet<RegistryConfig> registered = new ConcurrentSet<>();
    private final ConcurrentMap<Service, RegisterValue> registries = new ConcurrentHashMap<>();

    /**
     * consumer订阅的服务信息
     */
    private final ConcurrentSet<Service> subscribeSet = new ConcurrentSet<>();
    private final ConcurrentMap<Service, CopyOnWriteArrayList<NotifyListener>> subscribed =
            new ConcurrentHashMap<>();


    public AbstractRegistryService() {

        registerExecutor.execute(() -> {
            while(!shutdown.get()) {
                RegistryConfig RegistryConfig = null;
                try {
                    RegistryConfig = queue.take();
                    doRegister(RegistryConfig);
                } catch (InterruptedException e) {
                    logger.warn("addService executor interrupted");
                } catch (Throwable t) {
                    if(RegistryConfig != null) {
                        logger.error("service addService {} fail : {}", RegistryConfig.getService(), t);
                        final RegistryConfig meta = RegistryConfig;
                        scheduleExecutor.schedule(new Runnable() {

                            @Override
                            public void run() {
                                queue.add(meta);
                            }
                        },1,TimeUnit.SECONDS);
                    }
                }
            }
        });
    }

    @Override
    public void register(RegistryConfig registry) {
        queue.add(registry);
    }

    @Override
    public void unRegister(RegistryConfig registry) {
        if(!queue.remove(registry)) {
            doUnregister(registry);
        }
    }

    @Override
    public void subscribe(Service service, NotifyListener listener) {
        CopyOnWriteArrayList<NotifyListener> listeners =
                subscribed.computeIfAbsent(service,k->new CopyOnWriteArrayList<>());
        listeners.add(listener);
        subscribeSet.add(service);
        doSubscribe(service);
    }

    @Override
    public Collection<RegistryConfig> lookup(Service metadata) {
            RegisterValue value = registries.get(metadata);

            if (value == null) {
                return Collections.emptyList();
            }

            final Lock readLock = value.lock.readLock();
            readLock.lock();
            try {
                return Lists.newArrayList(value.metaSet);
            } finally {
                readLock.unlock();
            }
    }

    @Override
    public Map<Service, Integer> getConsumers() {
        return null;
    }

    @Override
    public Map<RegistryConfig, RegistryState> getProviders() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return shutdown.get();
    }

    @Override
    public void shutdownGracefully() {
        if(!shutdown.getAndSet(true)) {
            registerExecutor.shutdownNow();
            scheduleExecutor.shutdownNow();
        }
        destroy();
    }

    protected void notify(
            Service service, NotifyEvent event, RegistryConfig... array) {

        if (array == null || array.length == 0) {
            return;
        }
        CopyOnWriteArrayList<NotifyListener> listeners = subscribed.get(service);
        if (listeners != null) {
            for (NotifyListener l : listeners) {
                for (RegistryConfig registryConfig : array) {
                    l.notify(registryConfig, event);
                }
            }
        }
    }

    public abstract void doRegister(RegistryConfig RegistryConfig);

    public abstract void doSubscribe(Service registerMeta);

    public abstract void doUnregister(RegistryConfig RegistryConfig);


    public ConcurrentSet<RegistryConfig> getRegistered() {
        return registered;
    }

    public ConcurrentSet<Service> getSubscribeSet() {
        return subscribeSet;
    }


    protected static class RegisterValue {
        private long version = Long.MIN_VALUE;
        private final Set<RegistryConfig> metaSet = new HashSet<>();
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(); // segment-lock
    }

}
