package top.yunzhitan.config.support;

import com.yunzhitan.protocol.RpcProtocal;
import com.yunzhitan.registry.ServiceRegistry;
import com.yunzhitan.server.RpcServer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class RegistryConfig implements InitializingBean, DisposableBean{
    private String serverAddress;
    private String registryAddress;
    private String protocol;
    private AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();


    @Override
    public void destroy() {
        RpcServer ref = RpcServer.getInstance();
        ref.stop();

    }

    @Override
    public void afterPropertiesSet() {
        RpcServer ref = RpcServer.getInstance();
        ref.setServerAddress(serverAddress);
        RpcProtocal rpcProtocal = RpcProtocal.PROTOSTUFF;
        ref.setProtocal(rpcProtocal);
        ServiceRegistry registry = new ServiceRegistry(registryAddress);
        ref.setServiceRegistry(registry);
        ref.start();
    }

    public String getRegistryAddress() {
        return registryAddress;
    }

    public void setRegistryAddress(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public String getServerAddress() {
        return serverAddress;
    }

    public void setServerAddress(String registryAddress) {
        this.serverAddress = registryAddress;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public AnnotationConfigApplicationContext getContext() {
        return context;
    }

    public void setContext(AnnotationConfigApplicationContext context) {
        this.context = context;
    }
}
