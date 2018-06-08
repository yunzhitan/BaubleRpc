package top.yunzhitan.rpc.consumer.proxy;

import com.google.common.collect.Lists;
import top.yunzhitan.Util.Strings;
import top.yunzhitan.common.ServiceConfig;
import top.yunzhitan.registry.proxy.Proxies;
import top.yunzhitan.rpc.cluster.ClusterType;
import top.yunzhitan.rpc.consumer.loadbalance.LoadBalanceFactory;
import top.yunzhitan.rpc.consumer.loadbalance.LoadBalanceType;
import top.yunzhitan.rpc.consumer.transporter.DefaultTransporter;
import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.rpc.invoker.AsyncInvoker;
import top.yunzhitan.rpc.invoker.InvokerType;
import top.yunzhitan.rpc.invoker.SyncInvoker;
import top.yunzhitan.rpc.model.ClusterTypeConfig;
import top.yunzhitan.rpc.model.MethodSpecialConfig;
import top.yunzhitan.serialization.SerializerFactory;
import top.yunzhitan.serialization.SerializerType;
import top.yunzhitan.transport.Client;
import top.yunzhitan.transport.RemotePeer;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ProxyFactory<T> {

    private final Class<T> interfaceClass;

    //服务组别
    private String group;

    //服务名称
    private String serviceName;

    //服务版本
    private String version;

    //序列化方式
    private SerializerType serializerType;

    //负载均衡方式
    private LoadBalanceType loadBalanceType;

    //provider地址
    private List<RemotePeer> remotePeers;

    private Client client;

    private InvokerType invokeType = InvokerType.getDefault();

    // 调用超时时间设置
    private long timeoutMills;
    private List<MethodSpecialConfig> methodSpecialConfigs;
    private ClusterType clusterType = ClusterType.getDefault();

    // failover重试次数
    private int retries = 2;

    public static <T> ProxyFactory<T> factory(Class<T> interfaceClass) {
        ProxyFactory<T> factory = new ProxyFactory<>(interfaceClass);

        factory.remotePeers = Lists.newArrayList();
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
        this.serviceName = providerName;
        return this;
    }

    public ProxyFactory<T> version(String version) {
        this.version = version;
        return this;
    }

    public ProxyFactory<T> service(ServiceConfig serviceConfig) {
        return group(serviceConfig.getGroup())
                .providerName(serviceConfig.getServiceName())
                .version(serviceConfig.getVersion());
    }

    public ProxyFactory<T> client(Client client) {
        this.client = client;
        return this;
    }

    public ProxyFactory<T> serializerType(SerializerType serializerType) {
        this.serializerType = serializerType;
        return this;
    }

    public ProxyFactory<T> loadBalanceType(LoadBalanceType loadBalanceType) {
        this.loadBalanceType = loadBalanceType;
        return this;
    }


    public ProxyFactory<T> addProviderAddress(RemotePeer... remotePeers) {
        Collections.addAll(this.remotePeers, remotePeers);
        return this;
    }

    public ProxyFactory<T> addProviderAddress(List<RemotePeer> addresses) {
        this.remotePeers.addAll(addresses);
        return this;
    }

    public ProxyFactory<T> invokeType(InvokerType invokeType) {
        this.invokeType = invokeType;
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

        top.yunzhitan.rpc.Service annotation = interfaceClass.getAnnotation(top.yunzhitan.rpc.Service.class);

        if (annotation != null) {
            group = annotation.group();
            serviceName =  annotation.name();
        }

        ServiceConfig serviceConfig = new ServiceConfig(group, serviceName, version);

        checkArgument(Strings.isNotBlank(group), "group");
        checkArgument(Strings.isNotBlank(serviceName), "serviceName");
        checkNotNull(client, "consumerBoy");
        checkNotNull(serializerType, "serializerType");


        // transporter
        Transporter transporter = getTransporter(serializerType,client,loadBalanceType)
                .timeoutMillis(timeoutMills);

        ClusterTypeConfig strategyConfig = ClusterTypeConfig.of(clusterType, retries);
        Object handler;
        switch (invokeType) {
            case SYNC:
                handler = new SyncInvoker(client.getAppName(), serviceConfig, transporter, strategyConfig, methodSpecialConfigs);
                break;
            case ASYNC:
                handler = new AsyncInvoker(client.getAppName(), serviceConfig, transporter, strategyConfig, methodSpecialConfigs);
                break;
            default:
                handler = new AsyncInvoker(client.getAppName(), serviceConfig, transporter, strategyConfig, methodSpecialConfigs);
        }

        return Proxies.getDefault().newProxy(interfaceClass, handler);

    }

    private Transporter getTransporter(SerializerType serializerType,Client client,LoadBalanceType loadBalanceType) {
        return new DefaultTransporter(SerializerFactory.getSerializer(serializerType.value())
        ,client, LoadBalanceFactory.getLoadBalancer(loadBalanceType));
    }


}
