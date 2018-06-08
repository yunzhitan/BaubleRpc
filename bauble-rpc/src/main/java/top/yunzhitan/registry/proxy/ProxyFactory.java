package top.yunzhitan.registry.proxy;

import top.yunzhitan.rpc.invoker.Invoker;

public class ProxyFactory {

    public static <T> T buildProxy(String proxyType, Class<T> cls, Invoker invoker) {
        Proxies proxies = Proxies.parse(proxyType);
        return proxies.newProxy(cls,invoker);
    }
}
