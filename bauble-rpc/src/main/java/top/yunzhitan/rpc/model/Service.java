package top.yunzhitan.rpc.model;

import top.yunzhitan.transport.Directory;

import java.io.Serializable;
import java.util.Objects;

/**
 * 服务的元数据
 */

public class Service extends Directory implements Serializable {

    private static final long serialVersionUID = -237264327643278L;

    private String group; //服务组别
    private String serviceName; //服务名称
    private String version;   //服务版本号

    public Service() {
    }

    public Service(String group, String serviceName, String version) {
        this.group = group;
        this.serviceName = serviceName;
        this.version = version;
    }

    public Service(Directory directory) {
        this.group = directory.getGroup();
        this.serviceName = directory.getServiceName();
        this.version = directory.getVersion();
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

    public String getDirectory() {
        return String.format("%s/%s/%s",group,serviceName,version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service that = (Service) o;
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
        return "Service{" +
                "group='" + group + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
