package top.yunzhitan.registry;

import top.yunzhitan.rpc.cluster.Cluster;
import top.yunzhitan.rpc.model.ProviderConfig;

import java.util.List;

/**
 * 服务消费者各种活动的客户端类
 */
public interface ConsumerBoy<T> {

    /**
     * 调用一个服务
     *
     * @return 代理类
     */
    public abstract T refer();

    /**
     * 取消调用一个服务
     */
    public abstract void unRefer();

    /**
     * 拿到代理类
     *
     * @return 代理类
     */
    public abstract T getProxyIns();

    /**
     * 得到调用集群
     *
     * @return 服务端集群
     */
    public abstract Cluster getCluster();

    /**
     * 订阅服务列表
     *
     * @return 服务列表
     */
    public abstract List<ProviderConfig> subscribe();

    /**
     * 是否已经订阅完毕
     *
     * @return 是否订阅完毕
     */
    public abstract boolean isSubscribed();


}
