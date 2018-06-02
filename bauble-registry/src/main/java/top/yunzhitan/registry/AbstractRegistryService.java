package top.yunzhitan.registry;


import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.Util.collection.ConcurrentSet;
import top.yunzhitan.common.ServiceConfig;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstractRegistryService implements RegistryService {

    private static final Logger logger = LoggerFactory.getLogger(AbstractRegistryService.class);

    private final LinkedBlockingQueue<ProviderConfig> queue = new LinkedBlockingQueue<>();
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
    private final ConcurrentSet<ProviderConfig> registered = new ConcurrentSet<>();
    private final ConcurrentMap<ServiceConfig, RegisterValue> registries = new ConcurrentHashMap<>();

    /**
     * consumer订阅的服务信息
     */
    private final ConcurrentSet<ServiceConfig> subscribeSet = new ConcurrentSet<>();
    private final ConcurrentMap<ServiceConfig, CopyOnWriteArrayList<NotifyListener>> subscribed =
            new ConcurrentHashMap<>();


    public AbstractRegistryService() {

        registerExecutor.execute(() -> {
            while(!shutdown.get()) {
                ProviderConfig ProviderConfig = null;
                try {
                    ProviderConfig = queue.take();
                    doRegister(ProviderConfig);
                } catch (InterruptedException e) {
                    logger.warn("addService executor interrupted");
                } catch (Throwable t) {
                    if(ProviderConfig != null) {
                        logger.error("serviceConfig addService {} fail : {}", ProviderConfig.getServiceConfig(), t);
                        final ProviderConfig meta = ProviderConfig;
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
    public void register(ProviderConfig registry) {
        queue.add(registry);
    }

    @Override
    public void unRegister(ProviderConfig registry) {
        if(!queue.remove(registry)) {
            doUnregister(registry);
        }
    }

    @Override
    public void subscribe(ServiceConfig serviceConfig, NotifyListener listener) {
        CopyOnWriteArrayList<NotifyListener> listeners =
                subscribed.computeIfAbsent(serviceConfig, k->new CopyOnWriteArrayList<>());
        listeners.add(listener);
        subscribeSet.add(serviceConfig);
        doSubscribe(serviceConfig);
    }

    @Override
    public Collection<ProviderConfig> lookup(ServiceConfig metadata) {
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
    public Map<ServiceConfig, Integer> getConsumers() {
        return null;
    }

    @Override
    public Map<ProviderConfig, RegistryState> getProviders() {
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
            ServiceConfig serviceConfig, NotifyEvent event, ProviderConfig... array) {

        if (array == null || array.length == 0) {
            return;
        }
        CopyOnWriteArrayList<NotifyListener> listeners = subscribed.get(serviceConfig);
        if (listeners != null) {
            for (NotifyListener l : listeners) {
                for (ProviderConfig providerConfig : array) {
                    l.notify(providerConfig, event);
                }
            }
        }
    }

    public abstract void doRegister(ProviderConfig ProviderConfig);

    public abstract void doSubscribe(ServiceConfig registerMeta);

    public abstract void doUnregister(ProviderConfig ProviderConfig);


    public ConcurrentSet<ProviderConfig> getRegistered() {
        return registered;
    }

    public ConcurrentSet<ServiceConfig> getSubscribeSet() {
        return subscribeSet;
    }


    protected static class RegisterValue {
        private long version = Long.MIN_VALUE;
        private final Set<ProviderConfig> metaSet = new HashSet<>();
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(); // segment-lock
    }

}
