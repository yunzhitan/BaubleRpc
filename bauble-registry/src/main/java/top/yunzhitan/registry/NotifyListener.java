package top.yunzhitan.registry;


public interface NotifyListener {

    void notify(ProviderConfig providerConfig, NotifyEvent event);
}
