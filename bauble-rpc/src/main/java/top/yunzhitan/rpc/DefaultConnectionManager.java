package top.yunzhitan.rpc;

import io.netty.channel.Channel;
import top.yunzhitan.Util.BaubleServiceLoader;
import top.yunzhitan.Util.ThrowUtil;
import top.yunzhitan.registry.*;
import top.yunzhitan.rpc.model.Service;
import top.yunzhitan.transport.Client;
import top.yunzhitan.transport.Directory;
import top.yunzhitan.transport.FutureListener;
import top.yunzhitan.transport.RemotePeer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Collection;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.base.Preconditions.checkNotNull;

public class DefaultConnectionManager implements ConnectionManager {

    private Client client;
    private String appName;
    private RegistryService registryService;
    ConcurrentMap<Service, CopyOnWriteArrayList<RemotePeer>> addressGroups;
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
        this.appName = appName;
        this.client = client;
        this.registryService = BaubleServiceLoader.load(RegistryService.class).find(RegistryType.ZOOKEEPER.getValue());
    }

    @Override
    public String getAppname() {
        return appName;
    }

    @Override
    public boolean waitForAvailable(long timeoutMillis,Directory directory) {
        if (client.isDirectoryAvailable(directory)) {
            return true;
        }

        long remainTime = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);

        boolean available = false;
        final ReentrantLock _look = lock;
        _look.lock();
        try {
            signalNeeded.set(true);
            // avoid "spurious wakeup" occurs
            while (!(available = client.isDirectoryAvailable(directory))) {
                if ((remainTime = notifyCondition.awaitNanos(remainTime)) <= 0) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            ThrowUtil.throwException(e);
        } finally {
            _look.unlock();
        }

        return available || client.isDirectoryAvailable(directory);
    }

    public boolean waitAvailable(Directory directory,long timeoutMillis) {
        if(client.isDirectoryAvailable(directory)) {
            return true;
        }
        long remainTime = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        boolean available = false;
        ReentrantLock _lock = lock;
        _lock.lock();
        try{
            signalNeeded.set(true);
            while(! (available = client.isDirectoryAvailable(directory))) {
                if((remainTime = notifyCondition.awaitNanos(remainTime)) <= 0)
                    break;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            _lock.unlock();
        }

        return available || client.isDirectoryAvailable(directory);
    }


    @Override
    public ConnectionManager initialization(Class<?> interfaceClass, String version) {
        checkNotNull(interfaceClass, "interfaceClass");
        ServiceProvider annotation = interfaceClass.getAnnotation(ServiceProvider.class);
        checkNotNull(annotation, interfaceClass + " is not a ServiceProvider interface");
        String providerName = annotation.name();
        String group = annotation.group();
        providerName =  providerName;
        version = version;

        return initialization(new Service(group, providerName, version));

    }
    @Override
    public ConnectionManager initialization(Service service) {
        subscribe(service, new NotifyListener() {
            @Override
            public void notify(URL meta, NotifyEvent event) {
                SocketAddress address = new InetSocketAddress(meta.getHost(),meta.getPort());
                RemotePeer remotePeer = client.getRemotePeer(address);
                if(event == NotifyEvent.CHILD_ADDED) {
                    if(remotePeer.isAvailable()) {
                        onSucceed(remotePeer,service,signalNeeded.getAndSet(false));
                    }
                    else {
                        client.tryConnect(address, new FutureListener<Channel>() {
                            @Override
                            public void operationSuccess(Channel c) {
                                remotePeer.setAvailable(true);
                                onSucceed(remotePeer,service,signalNeeded.getAndSet(false));
                            }

                            @Override
                            public void operationFailure(Throwable cause) {
                                ThrowUtil.throwException(cause);
                            }
                        });
                    }
                }
                else if(event == NotifyEvent.CHILD_REMOVED) {
                    client.removeRemotePeer(service,remotePeer);
                }
            }
        });
    }

    @Override
    public RegistryService getRegistryService() {
        return registryService;
    }

    @Override
    public void subscribe(Service service, NotifyListener listener) {
        registryService.subscribe(service, listener);
    }

    @Override
    public void offlineListening(SocketAddress address, OfflineListener listener) {

    }

    @Override
    public void shutdownGracefully() {

    }

    @Override
    public void connectRegistryServer(String registryConfig) {
        registryService.connectRegistryServer(registryConfig);
    }

    private static Service toService(Directory directory) {
        Service service = new Service();
        service.setGroup(checkNotNull(directory.getGroup(), "group"));
        service.setServiceName(checkNotNull(directory.getServiceName(), "serviceProviderName"));
        service.setVersion(checkNotNull(directory.getVersion(), "version"));
        return service;
    }

    private void onSucceed(RemotePeer remotePeer, Directory directory,boolean doSignal) {
        client.addRemotePeer(directory,remotePeer);

        if (doSignal) {
            final ReentrantLock _look = lock;
            _look.lock();
            try {
                notifyCondition.signalAll();
            } finally {
                _look.unlock();
            }
        }
    }

}
