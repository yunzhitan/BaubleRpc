package top.yunzhitan.rpc.model;

import lombok.Data;
import top.yunzhitan.common.ServiceConfig;
import top.yunzhitan.registry.RegistryConfig;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;

import static top.yunzhitan.common.RpcConfigs.getBooleanValue;
import static top.yunzhitan.common.RpcConfigs.getStringValue;
import static top.yunzhitan.common.RpcOptions.*;

@Data
public abstract class InterfaceConfig<T,S extends InterfaceConfig> implements Serializable {

    private static final long  serialVersionUID = -8738729920479618L;


    /**
     * 应用名称
     */
    protected String appName;

    /**
     * 服务接口：做为服务唯一标识的组成部分<br>
     * 不管普通调用和泛化调用，都是设置实际的接口类名称，
     *
     * @see #uniqueId
     */
    protected String                                 interfaceId;

    /**
     * 服务标签：做为服务唯一标识的组成部分
     *
     * @see #interfaceId
     */
    protected String                                 uniqueId         = getStringValue(DEFAULT_UNIQUEID);

    /**
     * 过滤器配置实例
     */
    protected transient List<Filter> filterRef;

    /**
     * 过滤器配置别名，多个用逗号隔开
     */
    protected List<String>                           filter;

    /**
     * 注册中心配置，可配置多个
     */
    protected List<RegistryConfig>                   registry;

    /**
     * 方法配置，可配置多个
     */
    protected Map<String, MethodConfig> methods;

    /**
     * 默认序列化
     */
    protected String                                 serialization    = getStringValue(DEFAULT_SERIALIZATION);

    /**
     * 是否注册，如果是false只订阅不注册
     */
    protected boolean                                register         = getBooleanValue(SERVICE_REGISTER);

    /**
     * 是否订阅服务
     */
    protected boolean                                subscribe        = getBooleanValue(SERVICE_SUBSCRIBE);

    /**
     * 代理类型
     */
    protected String                                 proxy            = getStringValue(DEFAULT_PROXY);

    /**
     * 服务分组：不做为服务唯一标识的一部分
     * @deprecated 不再作为服务唯一标识，请直接使用 {@link #uniqueId} 代替
     */
    protected String                                 group            = getStringValue(DEFAULT_GROUP);
    /**
     * 服务版本：不做为服务唯一标识的一部分
     *
     * @see #interfaceId
     * @see #uniqueId
     * @deprecated 从5.4.0开始，不再作为服务唯一标识，请直接使用 {@link #uniqueId} 代替
     */
    protected String                                 version          = getStringValue(DEFAULT_VERSION);

    protected String                                 serviceName          = getStringValue(DEFAULT_SERVICENAME);


    /**
     * Mock实现类
     */
    protected transient T                            mockRef;

    /**
     * 自定义参数
     */
    protected Map<String, String>                    parameters;

    /*-------- 下面是方法级配置 --------*/

    /**
     * 接口下每方法的最大可并行执行请求数，配置-1关闭并发过滤器，等于0表示开启过滤但是不限制
     * 子类默认值不一样
     protected int concurrents = 0;*/


    /**
     * 是否开启mock
     */
    protected boolean                                mock;

    /**
     * 是否开启参数验证(jsr303)
     */
    protected boolean                                validation;

    /**
     * 压缩算法，为空则不压缩
     */
    protected String                                 compress;

    /*-------------配置项结束----------------*/

    /**
     * 方法名称和方法参数配置的map，不需要遍历list
     */
    protected transient volatile Map<String, Object> configValueCache = null;

    /**
     * 代理接口类，和T对应，主要针对泛化调用
     */
    protected transient volatile Class<?>               proxyClass;

    /**
     * Service Config
     */
    private ServiceConfig serviceConfig = new ServiceConfig(group,serviceName,version);



}
