package com.yunzhitan.test.app;

import com.yunzhitan.client.RPCFuture;
import com.yunzhitan.client.RpcClient;
import com.yunzhitan.client.proxy.IAsyncObjectProxy;
import com.yunzhitan.protocol.RpcProtocal;
import com.yunzhitan.registry.ServiceDiscovery;
import com.yunzhitan.test.client.HelloService;

import java.util.concurrent.TimeUnit;

public class BenchmarkAsync {
    public static void main(String[] args) throws InterruptedException {
        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("127.0.0.1:2181");
        final RpcClient rpcClient = new RpcClient(serviceDiscovery, RpcProtocal.PROTOSTUFF);

        int threadNum = 10;
        final int requestNum = 20;
        Thread[] threads = new Thread[threadNum];

        long startTime = System.currentTimeMillis();
        //benchmark for async call
        for (int i = 0; i < threadNum; ++i) {
            threads[i] = new Thread(() -> {
                for (int i1 = 0; i1 < requestNum; i1++) {
                    try {
                        IAsyncObjectProxy client = RpcClient.createAsync(HelloService.class);
                        RPCFuture helloFuture = client.call("hello", Integer.toString(i1));
                        String result = (String) helloFuture.get(3000, TimeUnit.MILLISECONDS);
                        //System.out.println(result);
                        if (!result.equals("Hello! " + i1))
                            System.out.println("error = " + result);
                    } catch (Exception e) {
                        System.out.println(e);
                    }
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        long timeCost = (System.currentTimeMillis() - startTime);
        String msg = String.format("Async call total-time-cost:%sms, req/s=%s", timeCost, ((double) (requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);

        rpcClient.stop();

    }
}
