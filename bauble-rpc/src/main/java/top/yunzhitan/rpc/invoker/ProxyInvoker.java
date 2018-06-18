package top.yunzhitan.rpc.invoker;

import top.yunzhitan.registry.ConsumerBoy;
import top.yunzhitan.rpc.cluster.Cluster;
import top.yunzhitan.rpc.model.ConsumerConfig;
import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.rpc.model.RpcResponse;

/**
 * 客户端引用的Invoker
 */
public class ProxyInvoker implements Invoker{

    /**
     * 对应的客户端信息
     */
    protected final ConsumerConfig consumerConfig;

    /**
     *
     */
    protected Cluster cluster;

    public ProxyInvoker(ConsumerBoy consumerBoy) {
        this.consumerConfig = consumerBoy.getConsumerConfig();
        // 构建客户端
        this.cluster = consumerBoy.getCluster();
    }

    /**
     * proxy拦截的调用
     *
     * @param request 请求消息
     * @return 调用结果
     */
    @Override
    public RpcResponse invoke(RpcRequest request) throws RuntimeException {
        RpcResponse response = null;
        Throwable throwable = null;
        try {
            RpcInternalContext.pushContext();
            RpcInternalContext context = RpcInternalContext.getContext();
            context.setProviderSide(false);
            // 包装请求
            try {
                // 得到结果
                response = cluster.invoke(request);
            } catch (Exception e) {
                throwable = e;
                throw e;
            }
            // 包装响应
            return response;
        } finally {
            RpcInternalContext.removeContext();
            RpcInternalContext.popContext();
        }
    }

}
