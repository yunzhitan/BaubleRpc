package top.yunzhitan.example;

import top.yunzhitan.rpc.model.ConsumerConfig;
import top.yunzhitan.service.BenchmarkTest;

public class NewTest {

    public static void main(String[] args) {
        ConsumerConfig<BenchmarkTest> consumerConfig = new ConsumerConfig<BenchmarkTest>();

        consumerConfig.setAddressHolder();
    }
}
