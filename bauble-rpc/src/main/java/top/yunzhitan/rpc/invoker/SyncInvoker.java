package top.yunzhitan.rpc.invoker;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.common.Service;
import top.yunzhitan.rpc.model.ClusterTypeConfig;
import top.yunzhitan.rpc.model.MethodSpecialConfig;

import java.lang.reflect.Method;
import java.util.List;

public class SyncInvoker extends AbstractInvoker {
    public SyncInvoker(String appName, Service metadata, Transporter transporter, ClusterTypeConfig defaultStrategy, List<MethodSpecialConfig> methodSpecialConfigs) {
        super(appName, metadata, transporter, defaultStrategy, methodSpecialConfigs);
    }

    @RuntimeType
    public Object invoke(@Origin Method method, @AllArguments Object[] args) throws Throwable{
        return doInvoke(method.getName(),args,method.getReturnType(),true);
    }
}
