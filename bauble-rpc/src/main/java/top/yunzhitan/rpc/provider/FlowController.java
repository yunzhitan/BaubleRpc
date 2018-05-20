package top.yunzhitan.rpc.provider;

public interface FlowController<T> {

    boolean flowControl(T t);
}

