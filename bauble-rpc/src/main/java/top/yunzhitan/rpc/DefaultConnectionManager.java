package top.yunzhitan.rpc;

import top.yunzhitan.Util.BaubleServiceLoader;
import top.yunzhitan.Util.ThrowUtil;
import top.yunzhitan.common.ServiceConfig;
import top.yunzhitan.registry.NotifyListener;
import top.yunzhitan.registry.RegistryService;
import top.yunzhitan.transport.Client;
import top.yunzhitan.transport.RemotePeer;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class DefaultConnectionManager implements ConnectionManager {

    private Client client;
    private String appName;
    private RegistryService registryService;
    ConcurrentMap<ServiceConfig, CopyOnWriteArrayList<RemotePeer>> addressGroups;
    private final AtomicBoolean signalNeeded = new AtomicBoolean(false);
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notifyCondition = lock.newCondition();

    public DefaultConnectionManager(String appName, RegistryService registryService,Client client) {
        this.appName = appName;
        this.registryService = registryService;
        this.client = client;
    }

    public DefaultConnectionManager(String appName, RegistryType registryType,Client client) {
        this.appName = appName;
        this.client = client;
        this.registryService = BaubleServiceLoader.load(RegistryService.class).find(registryType.getValue());
    }

    public DefaultConnectionManager(String appName,Client client) {
        this(appName,RegistryType.ZOOKEEPER,client);
    }

    @Override
    public String getAppname() {
        return appName;
    }

    @Override
    public Collection<ProviderConfig> lookup(ServiceConfig ServiceConfig) {
        return null;
    }

    @Override
    public boolean waitForAvailable(long timeoutMillis,ServiceConfig serviceConfig) {
        if (client.isServiceAvalible(serviceConfig)) {
            return true;
        }

        long remainTime = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);

        boolean available = false;
        final ReentrantLock _look = lock;
        _look.lock();
        try {
            signalNeeded.set(true);
            // avoid "spurious wakeup" occurs
            while (!(available = client.isServiceAvalible(serviceConfig))) {
                if ((remainTime = notifyCondition.awaitNanos(remainTime)) <= 0) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            ThrowUtil.throwException(e);
        } finally {
            _look.unlock();
        }

        return available || client.isServiceAvalible(serviceConfig);
    }

    @Override
    public void initialization(ServiceConfig serviceConfig) {
        if(client.getRemotePeerList(serviceConfig).size() != 0)
            return;
        subscribe(serviceConfig, (registryConfig, event) -> {
            RemotePeer remotePeer = client.getRemotePeer(registryConfig);
            if(event == NotifyEvent.CHILD_ADDED) {
                client.addRemotePeer(serviceConfig,remotePeer);
            }
            else if(event == NotifyEvent.CHILD_REMOVED) {
                client.removeRemotePeer(serviceConfig,remotePeer);
            }
        });
    }

    @Override
    public RegistryService getRegistryService() {
        return registryService;
    }

    @Override
    public void subscribe(ServiceConfig serviceConfig, NotifyListener listener) {
        registryService.subscribe(serviceConfig, listener);
    }

    @Override
    public void offlineListening(SocketAddress address, OfflineListener listener) {

    }

    @Override
    public void shutdownGracefully() {
        registryService.shutdownGracefully();
    }

    @Override
    public void connectRegistryServer(String registryConfig) {
        registryService.start(registryConfig);
    }

}
