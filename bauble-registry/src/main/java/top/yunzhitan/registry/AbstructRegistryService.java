package top.yunzhitan.registry;


import com.yunzhitan.Util.collection.ConcurrentSet;
import com.yunzhitan.rpc.model.ServiceMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class AbstructRegistryService implements RegistryService {

    private static final Logger logger = LoggerFactory.getLogger(AbstructRegistryService.class);

    private final LinkedBlockingQueue<RegisterMeta> queue = new LinkedBlockingQueue<>();
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
    private final ConcurrentSet<RegisterMeta> registerMetaSet = new ConcurrentSet<>();
    /**
     * consumer订阅的服务信息
     */
    private final ConcurrentSet<ServiceMeta> subcribeSet = new ConcurrentSet<>();

    public AbstructRegistryService() {

        registerExecutor.execute(new Runnable() {
            @Override
            public void run() {
                while(!shutdown.get()) {
                    RegisterMeta registerMeta = null;
                    try {
                        registerMeta = queue.take();
                        doRegister(registerMeta);
                    } catch (InterruptedException e) {
                        logger.warn("register executor interrupted");
                    } catch (Throwable t) {
                        if(registerMeta != null) {
                            logger.error("service register {} fail : {}", registerMeta.getServiceMeta(), t);
                            final RegisterMeta meta = registerMeta;
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
    public void register(RegisterMeta registry) {
        queue.add(registry);
    }

    @Override
    public void unRegister(RegisterMeta registry) {
        if(!queue.remove(registry)) {
            doUnregister(registry);
        }
    }

    @Override
    public void subscribe(ServiceMeta registry) {

    }

    @Override
    public Collection<RegisterMeta> lookup(ServiceMeta metadata) {
        return null;
    }

    @Override
    public Map<ServiceMeta, Integer> getConsumers() {
        return null;
    }

    @Override
    public Map<RegisterMeta, RegistryState> getProviders() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public void shutdownGracefully() {

    }

    protected void notify(ServiceMeta meta,NotifyEvent event,RegisterMeta... registerMetas) {

        if(registerMetas == null || registerMetas.length == 0) {
            return;
        }
    }

    public abstract void doRegister(RegisterMeta registerMeta);

    public abstract void doSubscribe(ServiceMeta registerMeta);

    public abstract void doUnregister(RegisterMeta registerMeta);


    public ConcurrentSet<RegisterMeta> getRegisterMetaSet() {
        return registerMetaSet;
    }

    public ConcurrentSet<ServiceMeta> getSubcribeSet() {
        return subcribeSet;
    }
}
