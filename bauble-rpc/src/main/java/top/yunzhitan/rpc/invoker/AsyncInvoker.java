package top.yunzhitan.rpc.invoker;

import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.rpc.model.Service;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.ClusterTypeConfig;
import top.yunzhitan.rpc.model.MethodSpecialConfig;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 异步调用
 */
public class AsyncInvoker extends AbstructInvoker {
    public AsyncInvoker(String appName,
                        Service metadata,
                        Transporter transporter,
                        ClusterTypeConfig defaultStrategy,
                        List<MethodSpecialConfig> methodSpecialConfigs) {
        super(appName, metadata, transporter, defaultStrategy, methodSpecialConfigs);
    }

    public void invoke(Method method,Object[] args) throws Throwable {
        Class<?> returnType = method.getReturnType();

        Object result = doInvoke(method.getName(), args, returnType, false);

        InvokeFutureContext.set((InvokeFuture<?>) result);
    }

}
