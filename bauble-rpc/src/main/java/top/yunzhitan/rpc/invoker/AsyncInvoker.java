package top.yunzhitan.rpc.invoker;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.common.Service;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.ClusterTypeConfig;
import top.yunzhitan.rpc.model.MethodSpecialConfig;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 异步调用
 */
public class AsyncInvoker extends AbstractInvoker {
    public AsyncInvoker(String appName,
                        Service metadata,
                        Transporter transporter,
                        ClusterTypeConfig defaultStrategy,
                        List<MethodSpecialConfig> methodSpecialConfigs) {
        super(appName, metadata, transporter, defaultStrategy, methodSpecialConfigs);
    }

    @RuntimeType
    public void invoke(@Origin Method method, @AllArguments Object[] args) throws Throwable {
        Class<?> returnType = method.getReturnType();

        Object result = doInvoke(method.getName(), args, returnType, false);

        InvokeFutureContext.setFuture((InvokeFuture<?>) result);
    }

}
