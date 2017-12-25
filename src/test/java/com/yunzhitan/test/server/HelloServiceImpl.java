package com.yunzhitan.test.server;

import com.yunzhitan.test.client.HelloService;
import com.yunzhitan.test.client.Person;
import com.yunzhitan.server.RpcService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "Hello! " + name;
    }

    @Override
    public String hello(Person person) {
        return "Hello! " + person.getFirstName() + " " + person.getLastName();
    }
}
