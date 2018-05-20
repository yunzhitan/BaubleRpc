package top.yunzhitan.example;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;
import org.openjdk.jmh.runner.options.TimeValue;
import top.yunzhitan.rpc.consumer.loadbalance.LoadBalanceType;
import top.yunzhitan.rpc.consumer.proxy.ProxyFactory;
import top.yunzhitan.serialization.SerializerType;
import top.yunzhitan.service.BenchmarkTest;
import top.yunzhitan.service.Page;
import top.yunzhitan.service.User;
import top.yunzhitan.transport.Client;
import top.yunzhitan.transport.ConnectFailedException;
import top.yunzhitan.transport.netty.NettyClient;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@State(Scope.Benchmark)
public class ClientTest {

    private final BenchmarkTest userService;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final int CONCURRENT = 4;
    private final Client client;
    private static long startTime;
    private static long endTime;

    public ClientTest() {
        client = new NettyClient("任务测试");
        client.init();
        client.connectRegistryServer("127.0.0.1:2181");
        client.setServiceConsumer(BenchmarkTest.class,"1.0.1");
        if(!client.waitForAvailable(1000)) {
            throw new ConnectFailedException();
        }
        userService = ProxyFactory.factory(BenchmarkTest.class)
                .version("1.0.1")
                .client(client)
                .loadBalanceType(LoadBalanceType.RANDOM)
                .timeoutMillis(2000)
                .serializerType(SerializerType.PROTO_STUFF)
                .newProxyInstance();
    }

    public static void main(String[] args) throws Exception{
        ClientTest clientTest = new ClientTest();

        System.out.println(clientTest.getUser());
        Options opt = new OptionsBuilder()//
                .include(Client.class.getSimpleName())//
                .warmupIterations(3)//
                .warmupTime(TimeValue.seconds(10))//
                .measurementIterations(3)//
                .measurementTime(TimeValue.seconds(10))//
                .threads(1)//
                .forks(1)//
                .build();

        new Runner(opt).run();

    }

    @Benchmark
    @BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
    @OutputTimeUnit(TimeUnit.SECONDS)
    public boolean existUser() {
        String email = String.valueOf(counter.getAndIncrement());
        return userService.existUser(email);
    }

    @Benchmark
    @BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
    @OutputTimeUnit(TimeUnit.SECONDS)
    public boolean createUser() {
        int id = counter.getAndIncrement();
        User user = userService.getUser(id);
        return userService.createUser(user);
    }

    @Benchmark
    @BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
    @OutputTimeUnit(TimeUnit.SECONDS)
    public User getUser() {
        int id = counter.getAndIncrement();
        return userService.getUser(id);
    }

    @Benchmark
    @BenchmarkMode({ Mode.Throughput, Mode.AverageTime, Mode.SampleTime })
    @OutputTimeUnit(TimeUnit.SECONDS)
    public Page<User> listUser() {
        int pageNo = counter.getAndIncrement();
        return userService.listUser(pageNo);
    }


    public void shutdownGracelly() {
        client.shutdownGracefully();
    }
}
