package top.yunzhitan.rpc;

import top.yunzhitan.Util.BaubleServiceLoader;
import top.yunzhitan.common.Constants;
import top.yunzhitan.registry.RegisterMeta;
import top.yunzhitan.registry.RegistryService;
import top.yunzhitan.registry.RegistryType;
import top.yunzhitan.rpc.model.ServiceMeta;
import top.yunzhitan.rpc.model.ServiceProvider;
import top.yunzhitan.rpc.provider.DefaultServiceContainer;
import top.yunzhitan.rpc.provider.ProviderInitializer;
import top.yunzhitan.rpc.provider.ProviderInterceptor;
import top.yunzhitan.rpc.provider.ServiceContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.transport.Directory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.Executor;

public class DefaultProviderManager implements ProviderManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProviderManager.class);

    /**
     * 服务发布接口，服务管理者与注册中心的交互类
     */
    private final RegistryService registryService;
    private InetSocketAddress socketAddress;

    /**
     * Provider本地容器
     */
    private final ServiceContainer providerContainer = new DefaultServiceContainer();

    /**
     * 全局拦截器
     */
    private ProviderInterceptor[] globalInterceptors;

    public DefaultProviderManager(InetSocketAddress address) {
        this(address,RegistryType.ZOOKEEPER);
    }

    public DefaultProviderManager(InetSocketAddress socketAddress,RegistryType registryType) {
        registryType = registryType == null ? RegistryType.ZOOKEEPER : registryType;
        this.registryService = BaubleServiceLoader.load(RegistryService.class).find(registryType.getValue());
        this.socketAddress = socketAddress;
    }

    @Override
    public void connectRegistryServer(String registryConfig) {
        registryService.connectRegistryServer(registryConfig);
    }

    @Override
    public RegistryService getRegistryService() {
        return registryService;
    }

    @Override
    public void setGlobalInterceptors(ProviderInterceptor... globalInterceptors) {
        this.globalInterceptors = globalInterceptors;
    }

    @Override
    public ServiceProvider findService(Directory directory) {
        return providerContainer.findService(directory.directory());
    }

    @Override
    public ServiceProvider removeService(Directory directory) {
        return providerContainer.removeService(directory.directory());
    }

    @Override
    public List<ServiceProvider> allRegisteredServices() {
        return providerContainer.getAllService();
    }

    @Override
    public void publish(ServiceProvider serviceWrapper) {
        ServiceMeta metadata = serviceWrapper.getMetadata();

        RegisterMeta meta = new RegisterMeta(socketAddress.getHostName(),socketAddress.getPort());
        meta.setGroup(metadata.getGroup());
        meta.setServiceName(metadata.getServiceName());
        meta.setVersion(metadata.getVersion());
        meta.setWeight(serviceWrapper.getWeight());
        meta.setConnCount(Constants.SUGGESTED_CONNECTION_COUNT);

        registryService.register(meta);

    }

    @Override
    public void publish(ServiceProvider... serviceProviders) {
        for(ServiceProvider provider : serviceProviders) {
            publish(provider);
        }
    }

    @Override
    public void register(ServiceProvider serviceProvider) {
        providerContainer.registerService(serviceProvider.getMetadata().toString(),serviceProvider);
    }


    @Override
    public void register(ServiceProvider... serviceProviders) {
        for(ServiceProvider serviceProvider : serviceProviders) {
            register(serviceProvider);
        }
    }

    @Override
    public <T> void publishWithInitializer(ServiceProvider serviceWrapper, ProviderInitializer<T> initializer) {

    }

    @Override
    public <T> void publishWithInitializer(ServiceProvider serviceWrapper, ProviderInitializer<T> initializer, Executor executor) {

    }

    @Override
    public void publishAll() {
        for(ServiceProvider provider : allRegisteredServices()) {
            publish(provider);
        }
    }

    @Override
    public void unpublish(ServiceProvider serviceWrapper) {
        ServiceMeta metadata = serviceWrapper.getMetadata();

        RegisterMeta meta = new RegisterMeta(socketAddress.getHostName(),socketAddress.getPort());
        meta.setGroup(metadata.getGroup());
        meta.setServiceName(metadata.getServiceName());
        meta.setVersion(metadata.getVersion());
        meta.setWeight(serviceWrapper.getWeight());
        meta.setConnCount(Constants.SUGGESTED_CONNECTION_COUNT);

        registryService.unRegister(meta);

    }

    @Override
    public void unpublishAll() {
        for(ServiceProvider provider : allRegisteredServices()) {
            unpublish(provider);
        }
    }

}
