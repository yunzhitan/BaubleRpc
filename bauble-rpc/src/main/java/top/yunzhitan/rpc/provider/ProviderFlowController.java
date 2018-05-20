package top.yunzhitan.rpc.provider;

import java.util.concurrent.atomic.AtomicInteger;

public class ProviderFlowController<V> implements FlowController<V>{

    private AtomicInteger flowCount = new AtomicInteger(0);

    @Override
    public boolean flowControl(V v) {
        return flowCount.getAndIncrement() >= 9999;
    }
}
