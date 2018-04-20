package top.yunzhitan.registry;


import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.Util.collection.ConcurrentSet;
import top.yunzhitan.rpc.model.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public abstract class AbstructRegistryService implements RegistryService {

    private static final Logger logger = LoggerFactory.getLogger(AbstructRegistryService.class);

    private final LinkedBlockingQueue<URL> queue = new LinkedBlockingQueue<>();
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
    private final ConcurrentSet<URL> URLSet = new ConcurrentSet<>();
    private final ConcurrentMap<Service, RegisterValue> registries = new ConcurrentHashMap<>();

    /**
     * consumer订阅的服务信息
     */
    private final ConcurrentSet<Service> subcribeSet = new ConcurrentSet<>();
    private final ConcurrentMap<Service, CopyOnWriteArrayList<NotifyListener>> subscribeListeners =
            new ConcurrentHashMap<>();


    public AbstructRegistryService() {

        registerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while(!shutdown.get()) {
                    URL URL = null;
                    try {
                        URL = queue.take();
                        doRegister(URL);
                    } catch (InterruptedException e) {
                        logger.warn("register executor interrupted");
                    } catch (Throwable t) {
                        if(URL != null) {
                            logger.error("service register {} fail : {}", URL.getService(), t);
                            final URL meta = URL;
                            scheduleExecutor.schedule(new Runnable() {

                                @Override
                                public void run() {
                                    queue.add(meta);
                                }
                            },1,TimeUnit.SECONDS);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void register(URL registry) {
        queue.add(registry);
    }

    @Override
    public void unRegister(URL registry) {
        if(!queue.remove(registry)) {
            doUnregister(registry);
        }
    }

    @Override
    public void subscribe(Service registry, NotifyListener listener) {
        CopyOnWriteArrayList<NotifyListener> listeners = subscribeListeners.get(registry);

        if(listeners == null) {
            CopyOnWriteArrayList<NotifyListener> newListeners = new CopyOnWriteArrayList<>();
            listeners = subscribeListeners.putIfAbsent(registry,newListeners);
            if(listeners == null) {
                listeners = newListeners;
            }
        }
        listeners.add(listener);
        subcribeSet.add(registry);
        doSubscribe(registry);
    }

    @Override
    public Collection<URL> lookup(Service metadata) {
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
    public Map<URL, RegistryState> getProviders() {
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
    }

    protected void notify(
            Service service, NotifyEvent event, URL... array) {

        if (array == null || array.length == 0) {
            return;
        }
        CopyOnWriteArrayList<NotifyListener> listeners = subscribeListeners.get(service);
        if (listeners != null) {
            for (NotifyListener l : listeners) {
                for (URL m : array) {
                    l.notify(m, event);
                }
            }
        }
    }

    public abstract void doRegister(URL URL);

    public abstract void doSubscribe(Service registerMeta);

    public abstract void doUnregister(URL URL);


    public ConcurrentSet<URL> getURLSet() {
        return URLSet;
    }

    public ConcurrentSet<Service> getSubcribeSet() {
        return subcribeSet;
    }


    protected static class RegisterValue {
        private long version = Long.MIN_VALUE;
        private final Set<URL> metaSet = new HashSet<>();
        private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock(); // segment-lock
    }

}
