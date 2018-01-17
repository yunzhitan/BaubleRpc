package com.yunzhitan.test.app;

import com.yunzhitan.client.RpcClient;
import com.yunzhitan.protocol.RpcProtocal;
import com.yunzhitan.registry.ServiceDiscovery;
import com.yunzhitan.service.HelloService;

public class Benchmark {

    public static void main(String[] args) throws InterruptedException {

        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("127.0.0.1:2181");
        final RpcClient rpcClient = new RpcClient(serviceDiscovery, RpcProtocal.PROTOSTUFF);

        int threadNum = 10;
        final int requestNum = 100;
        Thread[] threads = new Thread[threadNum];

        long startTime = System.currentTimeMillis();
        //benchmark for sync call
        for(int i = 0; i < threadNum; ++i){
            threads[i] = new Thread(() -> {
                for (int i1 = 0; i1 < requestNum; i1++) {
                    final HelloService syncClient = RpcClient.create(HelloService.class);
                    String result = syncClient.hello(Integer.toString(i1));
                    if (!result.equals("Hello! " + i1))
                        System.out.print("error = " + result);
                }
            });
            threads[i].start();
        }
        for (Thread thread : threads) {
            thread.join();
        }
        long timeCost = (System.currentTimeMillis() - startTime);
        String msg = String.format("Sync call total-time-cost:%sms, req/s=%s",timeCost,((double)(requestNum * threadNum)) / timeCost * 1000);
        System.out.println(msg);

        rpcClient.stop();
    }
}
