package top.yunzhitan.rpc;

import top.yunzhitan.Util.BaubleServiceLoader;
import top.yunzhitan.common.ServiceConfig;
import top.yunzhitan.registry.ProviderConfig;
import top.yunzhitan.registry.RegistryService;
import top.yunzhitan.registry.RegistryType;
import top.yunzhitan.rpc.provider.Provider;
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
    public Provider findService(ServiceConfig serviceConfig) {
        return providerContainer.findService(serviceConfig.getDirectory());
    }

    @Override
    public Provider removeService(ServiceConfig serviceConfig) {
        return providerContainer.removeService(serviceConfig.getDirectory());
    }

    @Override
    public List<Provider> allRegisteredServices() {
        return providerContainer.getAllService();
    }

    @Override
    public void publish(Provider provider) {
        ServiceConfig serviceConfig = provider.getServiceConfig();

        if(findService(serviceConfig) == null) {
            addServiceProvider(provider);
        }
        ProviderConfig providerConfig = new ProviderConfig(((InetSocketAddress)socketAddress).getHostName(),((InetSocketAddress)socketAddress).getPort());
        providerConfig.setServiceConfig(serviceConfig);
        providerConfig.setWeight(provider.getWeight());

        registryService.register(providerConfig);

    }

    @Override
    public void publish(Provider... providers) {
        for(Provider provider : providers) {
            publish(provider);
        }
    }

    private void addServiceProvider(Provider provider) {
        providerContainer.addService(provider.getServiceConfig().getDirectory(), provider);
    }


    private void addServiceProvider(Provider... providers) {
        for(Provider provider : providers) {
            addServiceProvider(provider);
        }
    }

    @Override
    public <T> void publishWithInitializer(Provider serviceWrapper, ProviderInitializer<T> initializer) {

    }

    @Override
    public <T> void publishWithInitializer(Provider serviceWrapper, ProviderInitializer<T> initializer, Executor executor) {

    }

    @Override
    public void publishAll() {
        for(Provider provider : allRegisteredServices()) {
            publish(provider);
        }
    }

    @Override
    public void unpublish(Provider serviceWrapper) {
        ServiceConfig serviceConfig = serviceWrapper.getServiceConfig();
        providerContainer.removeService(serviceConfig.getDirectory());

        ProviderConfig providerConfig = new ProviderConfig(((InetSocketAddress)socketAddress).getHostName(),((InetSocketAddress)socketAddress).getPort());
        providerConfig.setServiceConfig(serviceConfig);
        providerConfig.setWeight(serviceWrapper.getWeight());

        registryService.unRegister(providerConfig);

    }

    @Override
    public void unpublishAll() {
        for(Provider provider : allRegisteredServices()) {
            unpublish(provider);
        }
    }

    @Override
    public void shutdownGracefully() {
        registryService.shutdownGracefully();
    }
}
