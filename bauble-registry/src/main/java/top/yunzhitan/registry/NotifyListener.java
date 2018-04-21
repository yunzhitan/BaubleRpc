package top.yunzhitan.registry;


public interface NotifyListener {

    void notify(RegistryConfig registryConfig, NotifyEvent event);
}
