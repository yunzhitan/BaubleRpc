package top.yunzhitan.service;


import top.yunzhitan.rpc.Service;

@Service(group = "top.yunzhitan" , name="BenchmarkTest")
public interface BenchmarkTest {
     boolean existUser(String email);

     boolean createUser(User user);

     User getUser(long id);

     Page<User> listUser(int pageNo);

}
