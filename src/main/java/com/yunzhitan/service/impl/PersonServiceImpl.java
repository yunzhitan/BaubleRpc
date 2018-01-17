package com.yunzhitan.service.impl;

import com.yunzhitan.server.RpcService;
import com.yunzhitan.service.Person;
import com.yunzhitan.service.PersonService;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@RpcService(PersonService.class)
public class PersonServiceImpl implements PersonService {

    private static final Random random = new Random();
    @Override
    public List<Person> getTestPerson(String name, int num) {
        List<Person> persons = new ArrayList<>(num);
        for (int i = 0; i < num; ++i) {
            persons.add(new Person(Integer.toString(i), name));
        }
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return persons;
    }

    @Override
    public void pressTest() {
        try {
            Thread.sleep(random.nextInt(3000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
