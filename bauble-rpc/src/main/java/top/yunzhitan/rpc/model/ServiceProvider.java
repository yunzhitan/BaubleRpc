package top.yunzhitan.rpc.model;

import top.yunzhitan.Util.Pair;
import top.yunzhitan.rpc.provider.ProviderInterceptor;
import top.yunzhitan.rpc.service.ServiceImpl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

public class ServiceProvider {

    private Object serviceProvider;  //服务提供对象
    private ProviderInterceptor[] interceptors; //服务局部拦截器 可不设置
    private Class<?> interfaceClass; //接口类型
    private Service metadata; //服务元数据信息
    private int weight;           //权重   可不设置
    private Executor executor;       //服务私有线程池 用于延迟初始化设置 可不设置
    private Map<String, List<Pair<Class<?>[], Class<?>[]>>> extensions; //方法名称及参数，抛出异常类型


    public static final class ServiceProviderBuilder {
        private Object serviceProvider;  //服务提供对象
        private ProviderInterceptor[] interceptors; //拦截器
        private Class<?> interfaceClass; //接口类型
        private Service metadata; //服务元数据信息
        private int weight;           //权重
        private Executor executor;       //服务私有线程池
        private Map<String, List<Pair<Class<?>[], Class<?>[]>>> extensions;

        private ServiceProviderBuilder() {
        }

        public static ServiceProviderBuilder newServiceProvider() {
            return new ServiceProviderBuilder();
        }

        public ServiceProviderBuilder withServiceProvider(Object serviceProvider) {
            this.serviceProvider = serviceProvider;
            return this;
        }

        private void preBuild() {
            Class<?> providerClass = serviceProvider.getClass();

            ServiceImpl implAnnotation = null;
            top.yunzhitan.rpc.service.Service ifAnnotation = null;
            for (Class<?> cls = providerClass; cls != Object.class; cls = cls.getSuperclass()) {
                if (implAnnotation == null) {
                    implAnnotation = cls.getAnnotation(ServiceImpl.class);
                }

                Class<?>[] interfaces = cls.getInterfaces();
                if (interfaces != null) {
                    for (Class<?> i : interfaces) {
                        ifAnnotation = i.getAnnotation(top.yunzhitan.rpc.service.Service.class);
                        if (ifAnnotation == null) {
                            continue;
                        }

                        interfaceClass = i;
                        break;
                    }
                }

                if (implAnnotation != null && ifAnnotation != null) {
                    break;
                }
            }
            metadata = new Service(ifAnnotation.group(),ifAnnotation.name(),interfaceClass.getName());
            // method's extensions
            //
            // key:     method name
            // value:   pair.first:  方法参数类型(用于根据JLS规则实现方法调用的静态分派)
            //          pair.second: 方法显式声明抛出的异常类型
            extensions = new HashMap<>();
            for (Method method : interfaceClass.getMethods()) {
                String methodName = method.getName();
                List<Pair<Class<?>[], Class<?>[]>> list = extensions.get(methodName);
                if (list == null) {
                    list = new ArrayList<>();
                    extensions.put(methodName, list);
                }
                list.add(Pair.of(method.getParameterTypes(), method.getExceptionTypes()));
            }

        }

        public ServiceProviderBuilder withInterceptors(ProviderInterceptor[] interceptors) {
            this.interceptors = interceptors;
            return this;
        }

        public ServiceProviderBuilder withWeight(int weight) {
            this.weight = weight;
            return this;
        }

        public ServiceProviderBuilder withExecutor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public ServiceProvider build() {
            preBuild();
            ServiceProvider serviceProvider = new ServiceProvider();
            serviceProvider.interceptors = this.interceptors;
            serviceProvider.interfaceClass = this.interfaceClass;
            serviceProvider.executor = this.executor;
            serviceProvider.serviceProvider = this.serviceProvider;
            serviceProvider.weight = this.weight;
            return serviceProvider;
        }
    }

    public Object getServiceProvider() {
        return serviceProvider;
    }

    public ProviderInterceptor[] getInterceptors() {
        return interceptors;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public Service getMetadata() {
        return metadata;
    }

    public int getWeight() {
        return weight;
    }
}
