package top.yunzhitan.rpc.invoker;

import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.rpc.model.Service;
import top.yunzhitan.rpc.model.ClusterTypeConfig;
import top.yunzhitan.rpc.model.MethodSpecialConfig;

import java.lang.reflect.Method;
import java.util.List;

public class SyncInvoker extends AbstructInvoker {
    public SyncInvoker(String appName, Service metadata, Transporter transporter, ClusterTypeConfig defaultStrategy, List<MethodSpecialConfig> methodSpecialConfigs) {
        super(appName, metadata, transporter, defaultStrategy, methodSpecialConfigs);
    }

    public Object invoke(Method method,Object[] args) throws Throwable{
        return doInvoke(method.getName(),args,method.getReturnType(),true);
    }
}
