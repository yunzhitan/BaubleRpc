package top.yunzhitan.rpc;

import top.yunzhitan.Util.BaubleServiceLoader;
import top.yunzhitan.registry.RegistryConfig;
import top.yunzhitan.registry.RegistryService;
import top.yunzhitan.registry.RegistryType;
import top.yunzhitan.common.Service;
import top.yunzhitan.rpc.model.ServiceProvider;
import top.yunzhitan.rpc.provider.DefaultProviderContainer;
import top.yunzhitan.rpc.provider.ProviderInitializer;
import top.yunzhitan.rpc.provider.ProviderInterceptor;
import top.yunzhitan.rpc.provider.ProviderContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.Executor;

public class DefaultProviderManager implements ProviderManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultProviderManager.class);

    /**
     * 服务发布接口，服务管理者与注册中心的交互类
     */
    private final RegistryService registryService;
    private SocketAddress socketAddress;

    /**
     * Provider本地容器
     */
    private final ProviderContainer providerContainer = new DefaultProviderContainer();

    /**
     * 全局拦截器
     */
    private ProviderInterceptor[] globalInterceptors;

    public DefaultProviderManager(InetSocketAddress address) {
        this(address,RegistryType.ZOOKEEPER);
    }

    public DefaultProviderManager(SocketAddress socketAddress, RegistryType registryType) {
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
    public ServiceProvider findService(Service service) {
        return providerContainer.findService(service.getDirectory());
    }

    @Override
    public ServiceProvider removeService(Service service) {
        return providerContainer.removeService(service.getDirectory());
    }

    @Override
    public List<ServiceProvider> allRegisteredServices() {
        return providerContainer.getAllService();
    }

    @Override
    public void publish(ServiceProvider serviceProvider) {
        Service service = serviceProvider.getService();

        if(findService(service) == null) {
            addServiceProvider(serviceProvider);
        }
        RegistryConfig registryConfig = new RegistryConfig(((InetSocketAddress)socketAddress).getHostName(),((InetSocketAddress)socketAddress).getPort());
        registryConfig.setService(service);
        registryConfig.setWeight(serviceProvider.getWeight());

        registryService.register(registryConfig);

    }

    @Override
    public void publish(ServiceProvider... serviceProviders) {
        for(ServiceProvider provider : serviceProviders) {
            publish(provider);
        }
    }

    private void addServiceProvider(ServiceProvider serviceProvider) {
        providerContainer.addService(serviceProvider.getService().getDirectory(),serviceProvider);
    }


    private void addServiceProvider(ServiceProvider... serviceProviders) {
        for(ServiceProvider serviceProvider : serviceProviders) {
            addServiceProvider(serviceProvider);
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
        Service service = serviceWrapper.getService();
        providerContainer.removeService(service.getDirectory());

        RegistryConfig registryConfig = new RegistryConfig(((InetSocketAddress)socketAddress).getHostName(),((InetSocketAddress)socketAddress).getPort());
        registryConfig.setService(service);
        registryConfig.setWeight(serviceWrapper.getWeight());

        registryService.unRegister(registryConfig);

    }

    @Override
    public void unpublishAll() {
        for(ServiceProvider provider : allRegisteredServices()) {
            unpublish(provider);
        }
    }

    @Override
    public void shutdownGracefully() {
        registryService.shutdownGracefully();
    }
}
