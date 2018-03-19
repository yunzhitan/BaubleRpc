package top.yunzhitan.rpc.provider;

public interface ProviderInitializer<T> {

    /**
     * 初始化指定服务提供者
     * @param provider
     */
    void init (T provider);
}
