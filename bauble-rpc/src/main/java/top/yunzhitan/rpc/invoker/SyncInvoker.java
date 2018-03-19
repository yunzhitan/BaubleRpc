package top.yunzhitan.rpc.invoker;

import top.yunzhitan.rpc.model.ServiceMeta;
import top.yunzhitan.rpc.consumer.Dispatcher;
import top.yunzhitan.rpc.model.ClusterTypeConfig;
import top.yunzhitan.rpc.model.MethodSpecialConfig;

import java.lang.reflect.Method;
import java.util.List;

public class SyncInvoker extends AbstructInvoker {
    public SyncInvoker(String appName, ServiceMeta metadata, Dispatcher dispatcher, ClusterTypeConfig defaultStrategy, List<MethodSpecialConfig> methodSpecialConfigs) {
        super(appName, metadata, dispatcher, defaultStrategy, methodSpecialConfigs);
    }

    public void invoke(Method method,Object[] args) throws Throwable{
        doInvoke(method.getName(),args,method.getReturnType(),true);
    }
}
