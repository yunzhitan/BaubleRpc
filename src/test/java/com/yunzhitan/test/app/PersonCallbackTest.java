package com.yunzhitan.test.app;

import com.yunzhitan.client.AsyncRPCCallback;
import com.yunzhitan.client.RPCFuture;
import com.yunzhitan.client.RpcClient;
import com.yunzhitan.client.proxy.IAsyncObjectProxy;
import com.yunzhitan.protocol.RpcProtocal;
import com.yunzhitan.registry.ServiceDiscovery;
import com.yunzhitan.test.client.PersonService;
import com.yunzhitan.test.client.Person;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class PersonCallbackTest {
    public static void main(String[] args) {
        ServiceDiscovery serviceDiscovery = new ServiceDiscovery("127.0.0.1:2181");
        final RpcClient rpcClient = new RpcClient(serviceDiscovery, RpcProtocal.PROTOSTUFF);
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
            IAsyncObjectProxy client = rpcClient.createAsync(PersonService.class);
            int num = 5;
            RPCFuture helloPersonFuture = client.call("GetTestPerson", "xiaoming", num);
            helloPersonFuture.addCallback(new AsyncRPCCallback() {
                @Override
                public void success(Object result) {
                    List<Person> persons = (List<Person>) result;
                    for (Person person : persons) {
                        System.out.println(person);
                    }
                    countDownLatch.countDown();
                }

                @Override
                public void fail(Exception e) {
                    System.out.println(e);
                    countDownLatch.countDown();
                }
            });

        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        rpcClient.stop();

        System.out.println("End");
    }
}
