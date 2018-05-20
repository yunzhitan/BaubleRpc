package top.yunzhitan.example;


import top.yunzhitan.rpc.model.ServiceProvider;
import top.yunzhitan.service.BenchmarkTestImpl;
import top.yunzhitan.service.ServiceTestImpl;
import top.yunzhitan.transport.Server;
import top.yunzhitan.transport.netty.NettyServer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ServerExample {

    public static void main(String[] args){
        SocketAddress address = new InetSocketAddress("127.0.0.1",7662);
        Server server = new NettyServer(address);
        server.init();

        try {
            server.connectRegistryServer("127.0.0.1:2181");

            ServiceProvider provider1 = ServiceProvider.ServiceProviderBuilder.newServiceProvider()
                    .withServiceProvider(new ServiceTestImpl())
                    .withWeight(100)
                    .build();

            ServiceProvider provider2 = ServiceProvider.ServiceProviderBuilder.newServiceProvider()
                    .withServiceProvider(new BenchmarkTestImpl())
                    .withWeight(100)
                    .build();

            server.publish(provider1);
            server.publish(provider2);
            server.start();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> server.shutdownGracefully()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
