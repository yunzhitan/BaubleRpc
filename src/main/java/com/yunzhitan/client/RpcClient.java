package com.yunzhitan.client;

import com.yunzhitan.client.proxy.IAsyncObjectProxy;
import com.yunzhitan.client.proxy.ObjectProxy;
import com.yunzhitan.protocol.RpcProtocal;
import com.yunzhitan.registry.ServiceDiscovery;

import java.lang.reflect.Proxy;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("Convert2Diamond")
public class RpcClient {

    private final ServiceDiscovery serviceDiscovery;
    private static final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(65536));

    public RpcClient(ServiceDiscovery serviceDiscovery, RpcProtocal rpcProtocal) {
        this.serviceDiscovery = serviceDiscovery;
        ConnectManage.getInstance().setProtocal(rpcProtocal);
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ObjectProxy<T>(interfaceClass)
        );
    }

    public static <T> IAsyncObjectProxy createAsync(Class<T> interfaceClass) {
        return new ObjectProxy<T>(interfaceClass);
    }

    public static void submit(Runnable task){
        threadPoolExecutor.submit(task);
    }

    public void stop() {
        threadPoolExecutor.shutdown();
        serviceDiscovery.stop();
        ConnectManage.getInstance().stop();
    }
}

