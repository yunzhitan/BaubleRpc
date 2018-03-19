package top.yunzhitan.registry;


import top.yunzhitan.rpc.model.ServiceMeta;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Registry server register meta information.
 *
 * 注意: 不要轻易修改成员变量, 否则将影响hashCode和equals, RegisterMeta需要经常放入List, Map等容器中.
 *
 * jupiter
 * org.jupiter.registry
 *
 * @author jiachun.fjc
 */
public class RegisterMeta {

    // 地址
    private InetSocketAddress address;
    // metadata
    private ServiceMeta serviceMeta = new ServiceMeta();
    // 权重 hashCode() 与 equals() 不把weight计算在内
    private volatile int weight;
    // 建议连接数, jupiter客户端会根据connCount的值去建立对应数量的连接, hashCode() 与 equals() 不把connCount计算在内
    private volatile int connCount;

    public RegisterMeta(String host, int port) {
        address = new InetSocketAddress(host,port);
    }

    public String getHost() {
        return address.getHostName();
    }


    public int getPort() {
        return address.getPort();
    }


    public String getGroup() {
        return serviceMeta.getGroup();
    }

    public void setGroup(String group) {
        serviceMeta.setGroup(group);
    }

    public String getServiceName() {
        return serviceMeta.getServiceName();
    }

    public void setServiceName(String serviceProviderName) {
        serviceMeta.setServiceName(serviceProviderName);
    }

    public String getVersion() {
        return serviceMeta.getVersion();
    }

    public void setVersion(String version) {
        serviceMeta.setVersion(version);
    }

    public SocketAddress getAddress() {
        return address;
    }

    public ServiceMeta getServiceMeta() {
        return serviceMeta;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getConnCount() {
        return connCount;
    }

    public void setConnCount(int connCount) {
        this.connCount = connCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisterMeta that = (RegisterMeta) o;

        return !(address != null ? !address.equals(that.address) : that.address != null)
                && !(serviceMeta != null ? !serviceMeta.equals(that.serviceMeta) : that.serviceMeta != null);
    }

    @Override
    public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (serviceMeta != null ? serviceMeta.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RegisterMeta{" +
                "address=" + address +
                ", serviceMeta=" + serviceMeta +
                ", weight=" + weight +
                ", connCount=" + connCount +
                '}';
    }
}

