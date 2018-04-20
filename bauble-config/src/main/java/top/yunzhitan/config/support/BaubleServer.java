package top.yunzhitan.config.support;

import com.yunzhitan.registry.RegistryType;
import com.yunzhitan.rpc.DefaultProviderManager;
import com.yunzhitan.rpc.ProviderManager;
import org.springframework.beans.factory.InitializingBean;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class BaubleServer implements InitializingBean{

    private ProviderManager providerManager;
    private Server baubleServer;
    private String ipAddr;
    private int    boundPort;
    private RegistryType registryType;
    private String registryServerAddress; // 注册中心地址 [host1:port1,host2:port2....]

    @Override
    public void afterPropertiesSet() {
        SocketAddress address = new InetSocketAddress(ipAddr,boundPort);
        providerManager = new DefaultProviderManager(address,registryType);
    }
}
