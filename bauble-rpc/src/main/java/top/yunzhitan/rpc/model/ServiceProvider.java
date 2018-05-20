package top.yunzhitan.rpc.model;

import top.yunzhitan.Util.Pair;
import top.yunzhitan.Util.Reflects;
import top.yunzhitan.common.Service;
import top.yunzhitan.rpc.ServiceImpl;
import top.yunzhitan.rpc.filter.ServerFilterHandler;
import top.yunzhitan.rpc.invoker.ProviderContext;
import top.yunzhitan.rpc.provider.FlowController;
import top.yunzhitan.rpc.provider.ProviderInterceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ServiceProvider {

    private Object serviceProvider;  //服务对象
    private ProviderInterceptor[] interceptors; //服务局部拦截器
    private Class<?> interfaceClass; //接口类型
    private Service service; //服务信息
    private FlowController<RpcRequest> flowController; //私有流量控制
    private int weight;           //权重
    private Executor executor;
    private Map<String, List<Pair<Class<?>[], Class<?>[]>>> extensions; //方法名称及参数，抛出异常类型


    public Object getServiceProvider() {
        return serviceProvider;
    }

    public ProviderInterceptor[] getInterceptors() {
        return interceptors;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public Service getService() {
        return service;
    }

    public int getWeight() {
        return weight;
    }

    public Executor getExecutor() {
        return executor;
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public Object executeInvoke(RpcRequest request, ProviderContext context) {
        String methodName = request.getMethodName();
        Object[] args = request.getArguments();
        Object result = null;

        Class<?>[] expectCauseTypes = null;
        try {
            List<Pair<Class<?>[], Class<?>[]>> methodExtension = extensions.get(methodName);
            if (methodExtension == null) {
                throw new NoSuchMethodException(methodName);
            }

            // 根据JLS方法调用的静态分派规则查找最匹配的方法parameterTypes
            Pair<Class<?>[], Class<?>[]> bestMatch = Reflects.findMatchingParameterTypesExt(methodExtension, args);
            Class<?>[] parameterTypes = bestMatch.getFirst();
            expectCauseTypes = bestMatch.getSecond();

            result =  Reflects.fastInvoke(serviceProvider, methodName, parameterTypes, args);
        } catch (Throwable t) {
            context.setResult(t);
        }

        return result;
    }

    public ResultWrapper executeRequest(RpcRequest request) throws Throwable{         // stack copy

        ProviderContext invokeCtx = new ProviderContext(this);

        Object invokeResult = ServerFilterHandler.invoke(request, invokeCtx)
                    .getResult();

        ResultWrapper result = new ResultWrapper();
        result.setResult(invokeResult);
        return result;

    }

    public FlowController<RpcRequest> getFlowController() {
        return flowController;
    }

    public Map<String, List<Pair<Class<?>[], Class<?>[]>>> getExtensions() {
        return extensions;
    }

    public static final class ServiceProviderBuilder {
        private Object serviceProvider;  //服务提供对象
        private ProviderInterceptor[] interceptors; //拦截器
        private Class<?> interfaceClass; //接口类型
        private Service service; //服务元数据信息
        private FlowController<RpcRequest> flowController; //私有流量控制
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
            checkNotNull(serviceProvider, "serviceProvider");

            ServiceImpl implAnnotation = null;
            top.yunzhitan.rpc.Service ifAnnotation = null;
            for (Class<?> cls = providerClass; cls != Object.class; cls = cls.getSuperclass()) {
                if (implAnnotation == null) {
                    implAnnotation = cls.getAnnotation(ServiceImpl.class);
                }

                Class<?>[] interfaces = cls.getInterfaces();
                if (interfaces != null) {
                    for (Class<?> i : interfaces) {
                        ifAnnotation = i.getAnnotation(top.yunzhitan.rpc.Service.class);
                        if (ifAnnotation == null) {
                            continue;
                        }

                        checkArgument(
                                interfaceClass == null,
                                i.getName() + " has a @Service annotation, can't set [interfaceClass] again"
                        );

                        interfaceClass = i;
                        break;
                    }
                }

                if (implAnnotation != null && ifAnnotation != null) {
                    break;
                }
            }
            assert implAnnotation != null;
            assert ifAnnotation != null;
            service = new Service(ifAnnotation.group(),ifAnnotation.name(),implAnnotation.version());
            // method's extensions
            //
            // key:     method name
            // value:   pair.first:  方法参数类型(用于根据JLS规则实现方法调用的静态分派)
            //          pair.second: 方法显式声明抛出的异常类型
            extensions = new HashMap<>();
            for (Method method : interfaceClass.getMethods()) {
                String methodName = method.getName();
                List<Pair<Class<?>[], Class<?>[]>> list = extensions.computeIfAbsent(methodName,
                        k->new ArrayList<>());
                list.add(Pair.of(method.getParameterTypes(), method.getExceptionTypes()));
            }

        }

        public ServiceProviderBuilder withInterceptors(ProviderInterceptor[] interceptors) {
            this.interceptors = interceptors;
            return this;
        }

        public ServiceProviderBuilder withInterceptors(FlowController<RpcRequest> flowController) {
            this.flowController = flowController;
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
            serviceProvider.service = this.service;
            serviceProvider.flowController = this.flowController;
            serviceProvider.extensions = this.extensions;
            return serviceProvider;
        }
    }


}
