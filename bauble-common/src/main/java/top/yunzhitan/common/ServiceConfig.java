package top.yunzhitan.common;

import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * 服务的元数据
 */
@Data
public class ServiceConfig implements Serializable {

    private static final long serialVersionUID = -237264327643278L;

    private String group; //服务组别
    private String serviceName; //服务名称
    private String version;   //服务版本号

    public ServiceConfig() {
    }

    public ServiceConfig(String group, String serviceName, String version) {
        this.group = group;
        this.serviceName = serviceName;
        this.version = version;
    }



    public String getDirectory() {
        return String.format("%s/%s/%s",group,serviceName,version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServiceConfig that = (ServiceConfig) o;
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
        return "ServiceConfig{" +
                "group='" + group + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", version='" + version + '\'' +
                '}';
    }
}
