package top.yunzhitan.rpc.service;

public interface HelloService {
    String hello(String name);

    String hello(Person person);
}
