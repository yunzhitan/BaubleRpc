package top.yunzhitan.rpc;

import top.yunzhitan.Util.BaubleServiceLoader;
import top.yunzhitan.Util.SocketAddress;
import top.yunzhitan.registry.NotifyListener;
import top.yunzhitan.registry.RegisterMeta;
import top.yunzhitan.registry.RegistryService;
import top.yunzhitan.registry.RegistryType;
import top.yunzhitan.transport.Directory;

import java.util.Collection;

public class DefaultConsumerManager implements ConsunerManager{

    private String appName;
    private RegistryService registryService;

    public DefaultConsumerManager(String appName, RegistryService registryService) {
        this.appName = appName;
        this.registryService = registryService;
    }

    public DefaultConsumerManager(String appName, RegistryType registryType) {
        this.appName = appName;
        this.registryService = BaubleServiceLoader.load(RegistryService.class).find(registryType.getValue());
    }

    public DefaultConsumerManager(String appName) {
        this.appName = appName;
        this.registryService = BaubleServiceLoader.load(RegistryService.class).find(RegistryType.ZOOKEEPER.getValue());
    }

    @Override
    public String getAppname() {
        return null;
    }

    @Override
    public RegistryService getRegistryService() {
        return registryService;
    }

    @Override
    public Collection<RegisterMeta> lookup(Directory directory) {
        return null;
    }

    @Override
    public boolean awaitConnections(Class<?> interfaceClass, long timeoutMillis) {
        return false;
    }

    @Override
    public boolean awaitConnections(Class<?> interfaceClass, String version, long timeoutMillis) {
        return false;
    }

    @Override
    public boolean awaitConnections(Directory directory, long timeoutMillis) {
        return false;
    }

    @Override
    public void subscribe(Directory directory, NotifyListener listener) {

    }

    @Override
    public void offlineListening(SocketAddress address, OfflineListener listener) {

    }

    @Override
    public void shutdownGracefully() {

    }

    @Override
    public void connectRegistryServer(String registryConfig) {

    }
}
