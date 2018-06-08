package top.yunzhitan.registry;

import lombok.extern.slf4j.Slf4j;
import top.yunzhitan.common.CommonUtils;
import top.yunzhitan.common.StringUtils;
import top.yunzhitan.registry.proxy.ProxyFactory;
import top.yunzhitan.rpc.cluster.Cluster;
import top.yunzhitan.rpc.cluster.ClusterFactory;
import top.yunzhitan.rpc.invoker.Invoker;
import top.yunzhitan.rpc.invoker.InvokerFactory;
import top.yunzhitan.rpc.model.ConsumerConfig;
import top.yunzhitan.rpc.model.ProviderConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

@Slf4j
public class DefaultConsumerBoy<T> implements ConsumerBoy<T> {


    private ConsumerConfig<T> consumerConfig;

    /**
     * 代理实现类
     */
    private transient volatile T  proxyIns;

    /**
     * 代理的Invoker对象
     */
    private transient volatile Invoker invoker;

    /**
     * 集群管理类
     */
    protected transient volatile Cluster cluster;

    /**
     * 计数器
     */
    protected transient volatile CountDownLatch respondRegistries;


    public DefaultConsumerBoy(ConsumerConfig<T> consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    public T refer() {
        if(proxyIns != null) {
            return proxyIns;
        }

        String appName = consumerConfig.getAppName();
        String interfaceName = consumerConfig.getInterfaceId();

        if(log.isInfoEnabled()) {
            log.info(appName, "Refer consumer config : {} with bean id {}", interfaceName, consumerConfig.getConsumerId());
        }


        //build clusterType
        cluster = ClusterFactory.getCluster(consumerConfig,this);


        //build proxy
        invoker = InvokerFactory.getInvoker(consumerConfig);

        String proxyType = consumerConfig.getProxy();
        Class<?> interfaceClass = consumerConfig.getInterfaceClass();
        proxyIns = (T) ProxyFactory.buildProxy(proxyType,interfaceClass,invoker);

        return proxyIns;
    }

    @Override
    public void unRefer() {

    }

    @Override
    public T getProxyIns() {
        return proxyIns;
    }

    @Override
    public Cluster getCluster() {
        return cluster;
    }

    @Override
    public List<ProviderConfig> subscribe() {
        String directUrl = consumerConfig.getDirectUrl();
        List<ProviderConfig> result = null;

        //直连
        if(StringUtils.isNotBlank(directUrl)) {
            result = subscribeFromDirect(directUrl);
        } else {
            //注册中心
            List<RegistryConfig> registryConfigs = consumerConfig.getRegistry();
            if(CommonUtils.isNotEmpty(registryConfigs)) {
                result = subscribeFromRegistry(registryConfigs);
            }
        }

        return result;
    }

    @Override
    public boolean isSubscribed() {
        return false;
    }

    private List<ProviderConfig> subscribeFromDirect(String directUrl) {

    }

    private List<ProviderConfig> subscribeFromRegistry(List<RegistryConfig> registryConfigs) {
        List<ProviderConfig> providerList = new ArrayList<>();

        int addressWaitTime = consumerConfig.getAddressWaitTime();

        for(RegistryConfig registryConfig : registryConfigs) {
            RegistryService registryService = RegistryFactory.getRegistryService(registryConfig);

            providerList = registryService.subscribe(consumerConfig.getServiceConfig(), new NotifyListener() {
                @Override
                public void notify(ProviderConfig providerConfig, NotifyEvent event) {

                }
            });

            if(providerList != null)
                break;
        }
        return providerList;
    }
}
