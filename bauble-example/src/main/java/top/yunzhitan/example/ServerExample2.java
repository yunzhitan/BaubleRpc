package top.yunzhitan.example;

import top.yunzhitan.rpc.provider.Provider;
import top.yunzhitan.service.BenchmarkTestImpl;
import top.yunzhitan.service.ServiceTestImpl;
import top.yunzhitan.transport.Server;
import top.yunzhitan.transport.netty.NettyServer;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class ServerExample2 {
    public static void main(String[] args){
        SocketAddress address = new InetSocketAddress("127.0.0.1",7665);
        Server server = new NettyServer(address);
        server.init();

        try {
            server.connectRegistryServer("127.0.0.1:2181");

            Provider provider1 = Provider.ServiceProviderBuilder.newServiceProvider()
                    .withServiceProvider(new ServiceTestImpl())
                    .withWeight(100)
                    .build();

            Provider provider2 = Provider.ServiceProviderBuilder.newServiceProvider()
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
