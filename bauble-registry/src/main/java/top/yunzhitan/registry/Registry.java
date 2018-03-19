package top.yunzhitan.registry;

public interface Registry {

    /**
     * 连接注册中心，可连接多个地址  [host1:port1,host2:port2.....]
     * @param registryConfig
     */
    void connectRegistryServer(String registryConfig);
}
