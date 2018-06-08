package top.yunzhitan.rpc.cluster;

import lombok.Data;
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

@Data
public abstract class Cluster implements Invoker{

    private volatile boolean initiation = false;

    private ConsumerConfig consumerConfig;

    private ConsumerBoy consumerBoy;

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

        List<ProviderConfig> providerList = consumerBoy.subscribe();
        initiation = true;
    }

    public void checkInit() {
        if(!initiation)
            init();
    }
}
