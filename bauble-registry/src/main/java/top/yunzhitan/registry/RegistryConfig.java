package top.yunzhitan.registry;


import top.yunzhitan.Util.Strings;
import top.yunzhitan.rpc.model.Service;

import java.util.Objects;


public class RegistryConfig {

    // 注册的服务名称
    private Service service = new Service();
    private String host;
    private int port;
    // 服务提供者的权重
    private volatile int weight;

    public RegistryConfig(String host, int port) {
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
        return service.getGroup();
    }

    public void setGroup(String group) {
        service.setGroup(group);
    }

    public String getServiceName() {
        return service.getServiceName();
    }

    public void setServiceName(String serviceProviderName) {
        service.setServiceName(serviceProviderName);
    }

    public String getVersion() {
        return service.getVersion();
    }

    public void setVersion(String version) {
        service.setVersion(version);
    }


    public Service getService() {
        return service;
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
        if (!(o instanceof RegistryConfig)) return false;
        RegistryConfig registryConfig = (RegistryConfig) o;
        return getPort() == registryConfig.getPort() &&
                Objects.equals(getService(), registryConfig.getService()) &&
                Objects.equals(getHost(), registryConfig.getHost());
    }

    @Override
    public int hashCode() {

        return Objects.hash(getService(), getHost(), getPort());
    }

    @Override
    public String toString() {
        return "RegistryConfig{" +
                ", service=" + service +
                ", weight=" + weight +
                '}';
    }

    public static RegistryConfig parseRegistryConfig(String data) {
        //directory
        String[] array_1 = Strings.split(data,'/');
        String[] array_2 = Strings.split(array_1[5],':');
        String host = array_2[0];
        int port = Integer.parseInt(array_2[1]);
        RegistryConfig registryConfig = new RegistryConfig(host,port);
        registryConfig.setGroup(array_1[2]);
        registryConfig.setServiceName(array_1[3]);
        registryConfig.setVersion(array_1[4]);
        registryConfig.setWeight(Integer.parseInt(array_2[2]));

        return registryConfig;
    }

    public String getDirectory() {
        return String.format("%s/%s/%s",getGroup(),getServiceName(),getVersion());
    }
}

