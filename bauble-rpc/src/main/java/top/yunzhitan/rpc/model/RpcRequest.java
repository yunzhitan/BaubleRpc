package top.yunzhitan.rpc.model;

import lombok.Data;
import top.yunzhitan.common.ServiceConfig;

import java.io.Serializable;
import java.util.Arrays;

@Data
public class RpcRequest implements Serializable{

    private static final long serialVersionUID = -47365345647583434L;
    private String appName;

    private String methodName;

    private Object[] arguments;

    private ServiceConfig serviceConfig;


    @Override
    public String toString() {
        return "RpcRequest{" +
                "appName='" + appName + '\'' +
                ", methodName='" + methodName + '\'' +
                ", arguments=" + Arrays.toString(arguments) +
                '}';
    }
}