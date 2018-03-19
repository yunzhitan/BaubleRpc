package top.yunzhitan.rpc.service;

import java.util.List;

public interface PersonService {
    List<Person> getTestPerson(String name, int num);
    void pressTest();
}
