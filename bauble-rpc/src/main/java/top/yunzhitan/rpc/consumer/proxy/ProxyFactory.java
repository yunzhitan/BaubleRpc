package top.yunzhitan.rpc.consumer.proxy;

import com.google.common.collect.Lists;
import top.yunzhitan.Util.SocketAddress;
import top.yunzhitan.protocol.RpcProtocal;
import top.yunzhitan.rpc.ConsunerManager;
import top.yunzhitan.rpc.DispatchType;
import top.yunzhitan.rpc.cluster.ClusterType;
import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.rpc.invoker.AsyncInvoker;
import top.yunzhitan.rpc.invoker.InvokerType;
import top.yunzhitan.rpc.invoker.SyncInvoker;
import top.yunzhitan.rpc.model.ClusterTypeConfig;
import top.yunzhitan.rpc.model.MethodSpecialConfig;
import top.yunzhitan.rpc.model.ServiceMeta;
import top.yunzhitan.rpc.service.Service;
import top.yunzhitan.transport.Directory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;

public class ProxyFactory<T> {

    private final Class<T> interfaceClass;

    //服务组别
    private String group;

    //服务名称
    private String providerName;

    //服务版本
    private String version;

    //序列化方式
    private RpcProtocal serialType;

    //provider地址
    private List<SocketAddress> addresses;

    private ConsunerManager consunerManager;

    private InvokerType invokeType = InvokerType.getDefault();
    private DispatchType dispatchType;

    // 调用超时时间设置
    private long timeoutMills;
    private List<MethodSpecialConfig> methodSpecialConfigs;
    private ClusterType clusterType = ClusterType.getDefault();

    // failover重试次数
    private int retries = 2;

    public static <T> ProxyFactory<T> factory(Class<T> interfaceClass) {
        ProxyFactory<T> factory = new ProxyFactory<>(interfaceClass);

        factory.addresses = Lists.newArrayList();
        factory.methodSpecialConfigs = Lists.newArrayList();

        return factory;
    }

    private ProxyFactory(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public Class<T> getInterfaceClass() {
        return interfaceClass;
    }

    public ProxyFactory<T> group(String group) {
        this.group = group;
        return this;
    }

    public ProxyFactory<T> providerName(String providerName) {
        this.providerName = providerName;
        return this;
    }

    public ProxyFactory<T> version(String version) {
        this.version = version;
        return this;
    }

    public ProxyFactory<T> directory(Directory directory) {
        return group(directory.getGroup())
                .providerName(directory.getServiceProviderName())
                .version(directory.getVersion());
    }

    public ProxyFactory<T> client(ConsunerManager consunerManager) {
        this.consunerManager = consunerManager;
        return this;
    }

    public ProxyFactory<T> serializerType(Serial serializerType) {
        this.serialType = serializerType;
        return this;
    }


    public ProxyFactory<T> addProviderAddress(SocketAddress... addresses) {
        Collections.addAll(this.addresses, addresses);
        return this;
    }

    public ProxyFactory<T> addProviderAddress(List<SocketAddress> addresses) {
        this.addresses.addAll(addresses);
        return this;
    }

    public ProxyFactory<T> invokeType(InvokerType invokeType) {
        this.invokeType = invokeType;
        return this;
    }

    public ProxyFactory<T> dispatchType(DispatchType dispatchType) {
        this.dispatchType = dispatchType;
        return this;
    }

    public ProxyFactory<T> timeoutMillis(long timeoutMillis) {
        this.timeoutMills = timeoutMillis;
        return this;
    }

    public ProxyFactory<T> addMethodSpecialConfig(MethodSpecialConfig... methodSpecialConfigs) {
        Collections.addAll(this.methodSpecialConfigs, methodSpecialConfigs);
        return this;
    }

    public ProxyFactory<T> addHook(ConsumerHook... hooks) {
        Collections.addAll(this.hooks, hooks);
        return this;
    }

    public ProxyFactory<T> clusterType(ClusterType clusterType) {
        this.clusterType = clusterType;
        return this;
    }

    public ProxyFactory<T> failoverRetries(int retries) {
        this.retries = retries;
        return this;
    }

    public T newProxyInstance() {
        // check arguments

        Service annotation = interfaceClass.getAnnotation(Service.class);

        if (annotation != null) {
            group = annotation.group();
            String name = annotation.name();
            providerName =  name;
        }

        if (dispatchType == DispatchType.BROADCAST && invokeType == InvokerType.SYNC) {
            throw reject("broadcast & sync unsupported");
        }

        // metadata
        ServiceMeta metadata = new ServiceMeta(
                group,
                providerName,
                version
        );

        JConnector<JConnection> connector = consunerManager.connector();
        for (SocketAddress address : addresses) {
            connector.addChannelGroup(metadata, connector.group(address));
        }

        // transporter
        Transporter transporter = dispatcher()
                .hooks(hooks)
                .timeoutMillis(timeoutMillis)
                .methodSpecialConfigs(methodSpecialConfigs);

        ClusterTypeConfig strategyConfig = ClusterTypeConfig.of(clusterType, retries);
        Object handler;
        switch (invokeType) {
            case SYNC:
                handler = new SyncInvoker(consunerManager.getAppname(), metadata, transporter, strategyConfig, methodSpecialConfigs);
                break;
            case ASYNC:
                handler = new AsyncInvoker(consunerManager.getAppname(), metadata, transporter, strategyConfig, methodSpecialConfigs);
                break;
            default:
                throw new Throwable("invokeType: " + invokeType);
        }

        Object object = Proxy.newProxyInstance(
                interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass }, (InvocationHandler) handler);
        return interfaceClass.cast(object);

    }


}
