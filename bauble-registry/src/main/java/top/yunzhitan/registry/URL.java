package top.yunzhitan.registry;


import top.yunzhitan.Util.Strings;
import top.yunzhitan.rpc.model.Service;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


public class URL {

    // 地址
    private InetSocketAddress address;
    // metadata
    private Service service = new Service();
    private String host;
    private int port;
    // 权重 hashCode() 与 equals() 不把weight计算在内
    private volatile int weight;

    public URL(String host, int port) {
        this.host = host;
        this.port = port;
        address = new InetSocketAddress(host,port);
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

    public SocketAddress getAddress() {
        return address;
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
        if (o == null || getClass() != o.getClass()) return false;

        URL that = (URL) o;

        return !(address != null ? !address.equals(that.address) : that.address != null)
                && !(service != null ? !service.equals(that.service) : that.service != null);
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (service != null ? service.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "URL{" +
                "address=" + address +
                ", service=" + service +
                ", weight=" + weight +
                '}';
    }

    public static URL parseURL(String data) {
        //directory
        String[] array_1 = Strings.split(data,'/');
        String[] array_2 = Strings.split(array_1[5],':');
        String host = array_2[0];
        int port = Integer.parseInt(array_2[1]);
        URL meta = new URL(host,port);
        meta.setGroup(array_1[2]);
        meta.setServiceName(array_1[3]);
        meta.setVersion(array_1[4]);
        meta.setWeight(Integer.parseInt(array_2[2]));

        return meta;
    }
}

