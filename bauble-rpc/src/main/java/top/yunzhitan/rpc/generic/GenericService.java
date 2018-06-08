package top.yunzhitan.rpc.generic;

/**
 * 泛化调用的接口<br>
 *
 * @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a>
 * @author <a href="mailto:caojie.cj@antfin.com">CaoJie</a>
 */
public interface GenericService {

    /**
     * 泛化调用
     *
     * @param methodName 调用方法名
     * @param argTypes   参数类型
     * @param args       方法参数，参数不能是GenericObject类型
     * @return 正常类型（不能是GenericObject类型）
     */
    Object $invoke(String methodName, String[] argTypes, Object[] args);

    /**
     * 支持参数类型无法在类加载器加载情况的泛化调用
     *
     * @param methodName 调用方法名
     * @param argTypes   参数类型
     * @param args       方法参数,参数类型支持GenericObject
     * @return 除了JDK等内置类型，其它对象是GenericObject类型
     */
    Object $genericInvoke(String methodName, String[] argTypes, Object[] args);

    /**
     * 支持参数类型无法在类加载器加载情况的泛化调用
     *
     * @param methodName 调用方法名
     * @param argTypes   参数类型
     * @param args       方法参数,参数类型支持GenericObject
     * @param clazz      返回类型
     * @return 返回指定的T类型返回对象
     */
    <T> T $genericInvoke(String methodName, String[] argTypes, Object[] args, Class<T> clazz);


}
