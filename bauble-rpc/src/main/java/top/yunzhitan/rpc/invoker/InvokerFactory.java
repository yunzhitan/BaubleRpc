package top.yunzhitan.rpc.invoker;

import top.yunzhitan.Util.BaubleServiceLoader;
import top.yunzhitan.rpc.model.ConsumerConfig;

public class InvokerFactory {

    public static Invoker getInvoker(ConsumerConfig consumerConfig) {
        Invoker invoker = BaubleServiceLoader.load(Invoker.class).find(consumerConfig.getInvokeType());

        return invoker;
    }
}
