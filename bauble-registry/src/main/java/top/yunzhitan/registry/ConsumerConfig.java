package top.yunzhitan.registry;

import lombok.Data;
import top.yunzhitan.common.ServiceConfig;

import static top.yunzhitan.common.RpcConfigs.getIntValue;
import static top.yunzhitan.common.RpcOptions.*;

@Data
public class ConsumerConfig {

    /**
     * 长连接个数，不是所有的框架都支持一个地址多个长连接
     */
    protected int connectionNum      = getIntValue(CONSUMER_CONNECTION_NUM);

    /**
     * Consumer给Provider发心跳的间隔
     */
    protected int heartbeatPeriod    = getIntValue(CONSUMER_HEARTBEAT_PERIOD);

    /**
     * Consumer给Provider重连的间隔
     */
    protected int reconnectPeriod    = getIntValue(CONSUMER_RECONNECT_PERIOD);

    /**
     * 等待地址获取时间(毫秒)，-1表示等到拿到地址位置
     */
    protected int addressWait        = getIntValue(CONSUMER_ADDRESS_WAIT);

    /**
     * 客户端调用超时时间(毫秒)
     */
    protected int timeout            = getIntValue(CONSUMER_INVOKE_TIMEOUT);

    /**
     * The Retries. 失败后重试次数
     */
    protected int retries            = getIntValue(CONSUMER_RETRIES);

    /**
     * 观察者
     */
    private transient volatile NotifyListener notifyListener;

    private ServiceConfig serviceConfig;

}
