package top.yunzhitan.rpc.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * 服务的元数据
 */

public class ServiceMeta implements Serializable {

    private static final long serialVersionUID = -237264327643278L;

    private String group; //服务组别
    private String serviceName; //服务名称
    private String version;   //服务版本号

    public ServiceMeta() {
    }

    public ServiceMeta(String group, String serviceName, String version) {
        this.group = group;
        this.serviceName = serviceName;
        this.version = version;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceMeta that = (ServiceMeta) o;
        return Objects.equals(group, that.group) &&
                Objects.equals(serviceName, that.serviceName) &&
                Objects.equals(version, that.version);
    }

    @Override
    public int hashCode() {

        return Objects.hash(group, serviceName, version);
    }

    @Override
    public String toString() {
        return "ServiceMeta{" +
                "group='" + group + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
