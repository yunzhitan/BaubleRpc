package top.yunzhitan.rpc.cluster;

import lombok.extern.slf4j.Slf4j;
import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.rpc.model.ConsumerConfig;
import top.yunzhitan.rpc.model.ProviderConfig;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class ConnectionHolder {

    /**
     * 服务消费者配置
     */
    protected ConsumerConfig consumerConfig;

    /**
     * 构造函数
     *
     * @param consumerConfig 服务消费者配置
     */
    protected ConnectionHolder(ConsumerConfig consumerConfig) {
        this.consumerConfig = consumerConfig;
    }

    /**
     * 未初始化的（从未连接过，例如lazy=true）
     */
    private ConcurrentHashMap<ProviderConfig, Transporter> uninitializedConnections = new ConcurrentHashMap<>();

    /**
     * 存活的客户端列表（保持了长连接，且一切正常的）
     */
    private ConcurrentHashMap<ProviderConfig, Transporter> aliveConnections         = new ConcurrentHashMap<>();

    /**
     * 存活但是亚健康节点（连续心跳超时，这种只发心跳，不发请求）
     */
    private ConcurrentHashMap<ProviderConfig, Transporter> subHealthConnections     = new ConcurrentHashMap<>();

    /**
     * 失败待重试的客户端列表（连上后断开的）
     */
    private ConcurrentHashMap<ProviderConfig, Transporter> retryConnections         = new ConcurrentHashMap<>();

    /**
     * 客户端变化provider的锁
     */
    private Lock providerLock             = new ReentrantLock();

    /**
     * Gets retry connections.
     *
     * @return the retry connections
     */
    public ConcurrentHashMap<ProviderConfig, Transporter> getRetryConnections() {
        return retryConnections;
    }

    /**
     * Add alive.
     *
     * @param providerInfo the provider
     * @param transport    the transport
     */
    protected void addAlive(ProviderConfig providerInfo, Transporter transport) {
        if (checkState(providerInfo, transport)) {
            aliveConnections.put(providerInfo, transport);
        }
    }

    /**
     * Add retry.
     *
     * @param providerInfo the provider
     * @param transport    the transport
     */
    protected void addRetry(ProviderConfig providerInfo, Transporter transport) {
        retryConnections.put(providerInfo, transport);
    }

    /**
     * 从存活丢到重试列表
     *
     * @param providerInfo Provider
     * @param transport    连接
     */
    protected void aliveToRetry(ProviderConfig providerInfo, Transporter transport) {
        providerLock.lock();
        try {
            if (aliveConnections.remove(providerInfo) != null) {
                retryConnections.put(providerInfo, transport);
            }
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * 从重试丢到存活列表
     *
     * @param providerInfo Provider
     * @param transport    连接
     */
    protected void retryToAlive(ProviderConfig providerInfo, Transporter transport) {
        providerLock.lock();
        try {
            if (retryConnections.remove(providerInfo) != null) {
                if (checkState(providerInfo, transport)) {
                    aliveConnections.put(providerInfo, transport);
                }
            }
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * 检查状态是否可用
     *
     * @param providerInfo    服务提供者信息
     * @param clientTransport 客户端长连接
     * @return 状态是否可用
     */
    protected boolean checkState(ProviderConfig providerInfo, Transporter clientTransport) {
        //        Protocol protocol = ProtocolFactory.getProtocol(providerInfo.getProtocolType());
        //        ProtocolNegotiator negotiator = protocol.negotiator();
        //        if (negotiator != null) {
        //            return negotiator.handshake(providerInfo, clientTransport);
        //        } else {
        return true;
        //        }
    }

    /**
     * 从存活丢到亚健康列表
     *
     * @param providerInfo Provider
     * @param transport    连接
     */
    protected void aliveToSubHealth(ProviderConfig providerInfo, Transporter transport) {
        providerLock.lock();
        try {
            if (aliveConnections.remove(providerInfo) != null) {
                subHealthConnections.put(providerInfo, transport);
            }
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * 从亚健康丢到存活列表
     *
     * @param providerInfo Provider
     * @param transport    连接
     */
    protected void subHealthToAlive(ProviderConfig providerInfo, Transporter transport) {
        providerLock.lock();
        try {
            if (subHealthConnections.remove(providerInfo) != null) {
                if (checkState(providerInfo, transport)) {
                    aliveConnections.put(providerInfo, transport);
                }
            }
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * 从存活丢到亚健康列表
     *
     * @param providerInfo Provider
     * @param transport    连接
     */
    protected void subHealthToRetry(ProviderConfig providerInfo, Transporter transport) {
        providerLock.lock();
        try {
            if (subHealthConnections.remove(providerInfo) != null) {
                retryConnections.put(providerInfo, transport);
            }
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * 删除provider
     *
     * @param providerConfig the provider
     * @return 如果已经建立连接 ，返回ClientTransport
     */
    protected Transporter remove(ProviderConfig providerConfig) {
        providerLock.lock();
        try {
            Transporter transport = uninitializedConnections.remove(providerConfig);
            if (transport == null) {
                transport = aliveConnections.remove(providerConfig);
                if (transport == null) {
                    transport = subHealthConnections.remove(providerConfig);
                    if (transport == null) {
                        transport = retryConnections.remove(providerConfig);
                    }
                }
            }
            return transport;
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * 通知状态变成不可用,主要是：<br>
     * 1.注册中心删除，更新节点后变成不可用时<br>
     * 2.连接断线后（心跳+调用），如果是可用节点为空
     */
    public void notifyStateChangeToUnavailable() {
        final List<ConsumerStateListener> onAvailable = consumerConfig.getOnAvailable();
        if (onAvailable != null) {
            AsyncRuntime.getAsyncThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    // 状态变化通知监听器
                    for (ConsumerStateListener listener : onAvailable) {
                        try {
                            listener.onUnavailable(consumerConfig.getConsumerBootstrap().getProxyIns());
                        } catch (Exception e) {
                            log.error(consumerConfig.getAppName(),
                                    "Failed to notify consumer state listener when state change to unavailable");
                        }
                    }
                }
            });
        }
    }

    /**
     * 通知状态变成可用,主要是：<br>
     * 1.启动成功变成可用时<br>
     * 2.注册中心增加，更新节点后变成可用时<br>
     * 3.重连上从一个可用节点都没有变成有可用节点时
     */
    public void notifyStateChangeToAvailable() {
        final List<ConsumerStateListener> onAvailable = consumerConfig.getOnAvailable();
        if (onAvailable != null) {
            AsyncRuntime.getAsyncThreadPool().execute(new Runnable() {
                @Override
                public void run() {
                    // 状态变化通知监听器
                    for (ConsumerStateListener listener : onAvailable) {
                        try {
                            listener.onAvailable(consumerConfig.getConsumerBootstrap().getProxyIns());
                        } catch (Exception e) {
                            LOGGER.warnWithApp(consumerConfig.getAppName(),
                                    "Failed to notify consumer state listener when state change to available");
                        }
                    }
                }
            });
        }
    }


}
