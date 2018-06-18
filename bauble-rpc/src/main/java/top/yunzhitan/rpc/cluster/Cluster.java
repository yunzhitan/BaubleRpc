package top.yunzhitan.rpc.cluster;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import top.yunzhitan.common.CommonUtils;
import top.yunzhitan.common.StringUtils;
import top.yunzhitan.registry.ConsumerBoy;
import top.yunzhitan.rpc.consumer.loadbalance.LoadBalanceFactory;
import top.yunzhitan.rpc.consumer.loadbalance.LoadBalancer;
import top.yunzhitan.rpc.exception.RemoteException;
import top.yunzhitan.rpc.filter.FilterChain;
import top.yunzhitan.rpc.invoker.Invoker;
import top.yunzhitan.rpc.model.ConsumerConfig;
import top.yunzhitan.rpc.model.ProviderConfig;
import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.rpc.model.RpcResponse;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@Slf4j
public abstract class Cluster implements Invoker{

    private volatile boolean initialized = false;

    private volatile boolean destroyed = false;

    private ConsumerConfig consumerConfig;

    private ConsumerBoy consumerBoy;

    /**
     * 当前Client正在发送的调用数量
     */
    protected AtomicInteger countOfInvoke = new AtomicInteger(0);

    /**
     * 上一次连接的ProviderConfig
     */
    private volatile ProviderConfig lastProvider;



    /**
     * 负载均衡接口
     */
    protected LoadBalancer loadBalancer;

    /**
     * 连接管理器
     */
    protected ConnectionHolder connectionHolder;
    /**
     * 过滤器链
     */
    protected FilterChain filterChain;



    /**
     * 调用远程地址发送消息
     *
     * @param providerConfig 服务提供者信息
     * @param request      请求
     * @return 状态
     * @throws RemoteException Remote异常
     */
    public abstract RpcResponse sendMsg(ProviderConfig providerConfig, RpcRequest request) throws RemoteException;


    /**
     * 初始化操作
     */
    public void init() {

        loadBalancer = LoadBalanceFactory.getLoadBalancer(consumerConfig);
        connectionHolder = new ConnectionHolder(consumerConfig);
        List<ProviderConfig> providerList = null;

        try {
            providerList = consumerBoy.subscribe();
            if(CommonUtils.isNotEmpty(providerList)) {
                updateAllProviders(providerList);
            }
        } catch (Exception e) {
            log.error("Subscribe error! due to {}",e);
        }
        connectionHolder.init();
        initialized = true;
    }

    protected void checkClusterState() {
        if (destroyed) { // 已销毁
            throw new RuntimeException("Client has been destroyed!");
        }
        if (!initialized) { // 未初始化
            init();
        }
    }


    @Override
    public RpcResponse invoke(RpcRequest request){
        RpcResponse response = null;
        checkClusterState();

        try {
            countOfInvoke.incrementAndGet();
            response = doInvoke(request);
        } catch (Exception e) {
            log.error("Rpc error!! due to {}",e);
        } finally {
            countOfInvoke.decrementAndGet();
        }

        return response;

    }

    protected abstract RpcResponse doInvoke(RpcRequest msg);


    public void addProvider(ProviderConfig providerConfig) {

    }

    public void removeProvider(ProviderConfig providerConfig) {

    }

    public void updateAllProviders(List<ProviderConfig> providerConfigList) {

    }

    /**
     * 根据规则进行负载均衡
     *
     * @param message 调用对象
     * @return 一个可用的provider
     */
    protected ProviderConfig select(RpcRequest request)  {
        return select(request, null);
    }

    /**
     * 根据规则进行负载均衡
     *
     * @param request              调用对象
     * @param providerList 已调用列表
     * @return 一个可用的provider
     */
    protected ProviderConfig select(RpcRequest request, List<ProviderConfig> providerList)
    {
        // 粘滞连接，当前连接可用
        if (consumerConfig.isSticky()) {
            if (lastProvider != null) {
                ProviderConfig providerConfig = lastProvider;
                ClientTransport lastTransport = connectionHolder.getAvailableClientTransport(providerConfig);
                if (lastTransport != null && lastTransport.isAvailable()) {
                    return providerConfig;
                }
            }
        }
        // 原始服务列表数据 --> 路由结果
        List<ProviderConfig> providerInfos = routerChain.route(request, null);
        if (CommonUtils.isEmpty(providerInfos)) {
            throw new RuntimeException(request.getServiceConfig().getDirectory());
        }
        if (CommonUtils.isNotEmpty(providerList) && providerInfos.size() > providerList.size()) { // 总数大于已调用数
            providerInfos.removeAll(providerList);// 已经调用异常的本次不再重试
        }

        String targetIP = null;
        ProviderConfig providerInfo;
        RpcInternalContext context = RpcInternalContext.peekContext();
        if (context != null) {
            targetIP = (String) RpcInternalContext.getContext().getAttachment(RpcConstants.HIDDEN_KEY_PINPOINT);
        }
        if (StringUtils.isNotBlank(targetIP)) {
            // 如果指定了调用地址
            providerInfo = selectPinpointProvider(targetIP, providerInfos);
            if (providerInfo == null) {
                // 指定的不存在
                throw unavailableProviderException(request.getTargetServiceUniqueName(), targetIP);
            }
            ClientTransport clientTransport = selectByProvider(request, providerInfo);
            if (clientTransport == null) {
                // 指定的不存在或已死，抛出异常
                throw unavailableProviderException(request.getTargetServiceUniqueName(), targetIP);
            }
            return providerInfo;
        } else {
            do {
                // 再进行负载均衡筛选
                providerInfo = loadBalancer.select(request, providerInfos);
                ClientTransport transport = selectByProvider(request, providerInfo);
                if (transport != null) {
                    return providerInfo;
                }
                providerInfos.remove(providerInfo);
            } while (!providerInfos.isEmpty());
        }
        throw noAvailableProviderException(request.getTargetServiceUniqueName());
    }



}
