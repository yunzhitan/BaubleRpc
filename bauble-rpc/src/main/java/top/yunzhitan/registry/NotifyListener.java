package top.yunzhitan.registry;


import top.yunzhitan.rpc.model.ProviderConfig;

public interface NotifyListener {

    void notify(ProviderConfig providerConfig, NotifyEvent event);
}
