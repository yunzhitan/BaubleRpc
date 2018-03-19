package top.yunzhitan.rpc.invoker;

import top.yunzhitan.rpc.model.Response;

public interface Invoker {

    /**
     * get service Interface
     */
    Response invoke(String methodName,Object[] args);
}
