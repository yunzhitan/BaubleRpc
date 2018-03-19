package top.yunzhitan.registry;

import com.yunzhitan.Util.SocketAddress;
import com.yunzhitan.rpc.model.ServiceMeta;

public class RegistryWrapper {
    private SocketAddress address = new SocketAddress();
    private ServiceMeta serviceMeta = new ServiceMeta();
    //权重
    private volatile int weight;
    //建议连接数
    private volatile int connCount;

    public void setHost(String host) {
        address.setHost(host);
    }

    public String getHost() {
        return address.getHost();
    }

    public void setPort(int port) {
        address.setPort(port);
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

    public String getServiceProviderName() {
        return serviceMeta.getServiceName();
    }

    public void setServiceProviderName(String serviceProviderName) {
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

}
