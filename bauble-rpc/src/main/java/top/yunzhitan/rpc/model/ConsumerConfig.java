package top.yunzhitan.rpc.model;

import lombok.Data;
import top.yunzhitan.Util.id.IdWorker;
import top.yunzhitan.common.ClassUtils;
import top.yunzhitan.common.StringUtils;
import top.yunzhitan.registry.ConsumerBoy;
import top.yunzhitan.registry.DefaultConsumerBoy;
import top.yunzhitan.registry.NotifyListener;
import top.yunzhitan.rpc.generic.GenericService;

import java.io.Serializable;

import static top.yunzhitan.common.RpcConfigs.getBooleanValue;
import static top.yunzhitan.common.RpcConfigs.getIntValue;
import static top.yunzhitan.common.RpcConfigs.getStringValue;
import static top.yunzhitan.common.RpcOptions.*;

@Data
public class ConsumerConfig<T> extends InterfaceConfig<T,ConsumerConfig<T>> implements Serializable{

    private static final long serialVersionUID = 213843147876842384L;

    /**
     * 直连调用地址
     */
    protected String directUrl;

    /**
     * 是否泛化调用
     */
    protected boolean  generic;

    /**
     * 是否异步调用
     */
    protected String invokeType         = getStringValue(CONSUMER_INVOKE_TYPE);

    /**
     * 连接超时时间
     */
    protected int connectTimeout     = getIntValue(CONSUMER_CONNECT_TIMEOUT);

    /**
     * 关闭超时时间（如果还有请求，会等待请求结束或者超时）
     */
    protected int disconnectTimeout  = getIntValue(CONSUMER_DISCONNECT_TIMEOUT);

    protected int addressWaitTime = getIntValue(CONSUMER_ADDRESS_WAIT);

    /**
     * 集群处理，默认是failover
     */
    protected String clusterType = getStringValue(CONSUMER_CLUSTER);

    /**
     * The ConnectionHolder 连接管理器
     */
    protected String connectionHolder   = getStringValue(CONSUMER_CONNECTION_HOLDER);

    /**
     * 地址管理器
     */
    protected String addressHolder      = getStringValue(CONSUMER_ADDRESS_HOLDER);

    /**
     * 负载均衡
     */
    protected String loadBalancer       = getStringValue(CONSUMER_LOAD_BALANCER);

    /**
     * 是否延迟建立长连接（第一次调用时新建，注意此参数可能和check冲突，开启check后lazy自动失效）
     *
     * @see ConsumerConfig#check
     */
    protected boolean lazy               = getBooleanValue(CONSUMER_LAZY);

    /**
     * 粘滞连接，一个断开才选下一个
     * change transport when current is disconnected
     */
    protected boolean sticky             = getBooleanValue(CONSUMER_STICKY);

    /**
     * 是否jvm内部调用（provider和consumer配置在同一个jvm内，则走本地jvm内部，不走远程）
     */
    protected boolean inJVM              = getBooleanValue(CONSUMER_INJVM);

    /**
     * 是否强依赖（即没有服务节点就启动失败，注意此参数可能和lazy冲突，开启check后lazy自动失效)
     *
     * @see ConsumerConfig#lazy
     */
    protected boolean check              = getBooleanValue(CONSUMER_CHECK);

    /**
     * 长连接个数，不是所有的框架都支持一个地址多个长连接
     */
    protected int connectionNum      = getIntValue(CONSUMER_CONNECTION_NUM);

    /**
     * Consumer给Provider发心跳的间隔
     */
    protected int  heartbeatPeriod    = getIntValue(CONSUMER_HEARTBEAT_PERIOD);

    /**
     * Consumer给Provider重连的间隔
     */
    protected int reconnectPeriod    = getIntValue(CONSUMER_RECONNECT_PERIOD);

    /**
     * 观察者
     */
    private transient volatile NotifyListener notifyListener;

    /**
     * 客户端调用超时时间(毫秒)
     */
    protected int timeout            = getIntValue(CONSUMER_INVOKE_TIMEOUT);

    /**
     * The Retries. 失败后重试次数
     */
    protected int retries            = getIntValue(CONSUMER_RETRIES);

    /**
     * 代理类
     */
    protected ConsumerBoy<T> defaultConsumerBoy = new DefaultConsumerBoy<>(this);

    private Long consumerId = IdWorker.getInstance().nextId();


    /**
     * Is generic boolean.
     *
     * @return the boolean
     */
    public boolean isGeneric() {
        return generic;
    }


    /**
     * Is lazy boolean.
     *
     * @return the boolean
     */
    public boolean isLazy() {
        return lazy;
    }


    /**
     * Is sticky boolean.
     *
     * @return the boolean
     */
    public boolean isSticky() {
        return sticky;
    }


    /**
     * Is in jvm boolean.
     *
     * @return the boolean
     */
    public boolean isInJVM() {
        return inJVM;
    }


    /**
     * Is check boolean.
     *
     * @return the boolean
     */
    public boolean isCheck() {
        return check;
    }



    public T refer() {
        return defaultConsumerBoy.refer();
    }

    public Class<?> getInterfaceClass() {
        if (proxyClass != null) {
            return proxyClass;
        }
        if (generic) {
            return GenericService.class;
        }
        try {
            if (StringUtils.isNotBlank(interfaceId)) {
                this.proxyClass = ClassUtils.forName(interfaceId);
                if (!proxyClass.isInterface()) {
                    throw new RuntimeException("consumer.interface, not implement class");
                }
            } else {
                throw new RuntimeException("consumer.interface null interfaceId must be not null");
            }
        } catch (RuntimeException t) {
            throw new IllegalStateException(t.getMessage(), t);
        }
        return proxyClass;
    }


}
