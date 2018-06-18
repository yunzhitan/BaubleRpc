package top.yunzhitan.transport;

import lombok.Data;
import top.yunzhitan.rpc.listener.ChannelListener;
import top.yunzhitan.rpc.model.ConsumerConfig;
import top.yunzhitan.rpc.model.ProviderConfig;

import java.util.List;

import static top.yunzhitan.common.RpcConfigs.*;
import static top.yunzhitan.common.RpcOptions.*;

@Data
public class ClientTransportConfig {

    /**
     * 客户端的一些信息（请只读）
     */
    private ConsumerConfig consumerConfig;
    /**
     * 对应的Provider信息（请只读）
     */
    private ProviderConfig providerConfig;
    /**
     * 默认传输实现（一般和协议一致）
     */
    private String                container         = getStringValue(DEFAULT_TRANSPORT);
    /**
     * 默认连接超时时间
     */
    private int                   connectTimeout    = getIntValue(CONSUMER_CONNECT_TIMEOUT);
    /**
     * 默认断开连接超时时间
     */
    private int                   disconnectTimeout = getIntValue(CONSUMER_DISCONNECT_TIMEOUT);
    /**
     * 默认的调用超时时间（长连接调用时会被覆盖）
     */
    private int                   invokeTimeout     = getIntValue(CONSUMER_INVOKE_TIMEOUT);
    /**
     * 默认一个地址建立长连接的数量
     */
    private int                   connectionNum     = getIntValue(CONSUMER_CONNECTION_NUM);
    /**
     * 最大数据量
     */
    private int                   payload           = getIntValue(TRANSPORT_PAYLOAD_MAX);
    /**
     * 是否使用Epoll
     */
    private boolean               useEpoll          = getBooleanValue(TRANSPORT_USE_EPOLL);
    /**
     * 连接事件监听器
     */
    private List<ChannelListener> channelListeners;

