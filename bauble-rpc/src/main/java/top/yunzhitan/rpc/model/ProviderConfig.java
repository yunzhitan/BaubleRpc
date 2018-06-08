package top.yunzhitan.rpc.model;


import top.yunzhitan.Util.Strings;
import top.yunzhitan.common.ServiceConfig;

import java.util.Objects;


public class ProviderConfig {

    // 注册的服务名称
    private ServiceConfig serviceConfig;
    private String host;
    private int port;
    // 服务提供者的权重
    private volatile int weight;

    public ProviderConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getHost() {
        return host;
    }


    public int getPort() {
        return port;
    }


    public String getGroup() {
        return serviceConfig.getGroup();
    }

    public String getServiceName() {
        return serviceConfig.getServiceName();
    }

    public String getVersion() {
        return serviceConfig.getVersion();
    }

    public void setServiceConfig(ServiceConfig serviceConfig) {
        this.serviceConfig = serviceConfig;
    }

    public ServiceConfig getServiceConfig() {
        return serviceConfig;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProviderConfig)) return false;
        ProviderConfig providerConfig = (ProviderConfig) o;
        return getPort() == providerConfig.getPort() &&
                Objects.equals(getServiceConfig(), providerConfig.getServiceConfig()) &&
                Objects.equals(getHost(), providerConfig.getHost());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getServiceConfig(), getHost(), getPort());
    }

    @Override
    public String toString() {
        return "ProviderConfig{" +
                ", serviceConfig=" + serviceConfig +
                ", weight=" + weight +
                '}';
    }

    public static ProviderConfig parseRegistryConfig(String data) {
        //directory
        String[] array_1 = Strings.split(data,'/');
        String[] array_2 = Strings.split(array_1[5],':');
        String host = array_2[0];
        int port = Integer.parseInt(array_2[1]);
        ProviderConfig providerConfig = new ProviderConfig(host,port);
        ServiceConfig serviceConfig = new ServiceConfig(array_1[2],array_1[3],array_1[4]);
        providerConfig.setServiceConfig(serviceConfig);
        providerConfig.setWeight(Integer.parseInt(array_2[2]));

        return providerConfig;
    }

    public String getDirectory() {
        return String.format("%s/%s/%s",getGroup(),getServiceName(),getVersion());
    }
}

