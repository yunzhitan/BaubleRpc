package com.yunzhitan.boot;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class BootStrap {

    public static void main(String[] args) {
        new ClassPathXmlApplicationContext("test-server-spring.xml");
    }
}
