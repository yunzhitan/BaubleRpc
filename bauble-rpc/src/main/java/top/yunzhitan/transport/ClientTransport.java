package top.yunzhitan.transport;

import top.yunzhitan.rpc.exception.RpcException;
import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.rpc.model.RpcResponse;
import top.yunzhitan.transport.AbstractChannel;

import java.net.InetSocketAddress;

public abstract class ClientTransport {

    /**
     * 客户端配置
     */
    protected ClientTransportConfig transportConfig;

    /**
     * 客户端配置
     *
     * @param transportConfig 客户端配置
     */
    protected ClientTransport(ClientTransportConfig transportConfig) {
        this.transportConfig = transportConfig;
    }

    /**
     * 返回配置
     *
     * @return config
     */
    public ClientTransportConfig getConfig() {
        return transportConfig;
    }

    /**
     * 建立长连接
     */
    public abstract void connect();

    /**
     * 断开连接
     */
    public abstract void disconnect();

    /**
     * 销毁（最好是通过工厂模式销毁，这样可以清理缓存）
     */
    public abstract void destroy();

    /**
     * 是否可用（有可用的长连接）
     *
     * @return the boolean
     */
    public abstract boolean isAvailable();

    /**
     * 设置长连接
     *
     * @param channel the channel
     */
    public abstract void setChannel(AbstractChannel channel);

    /**
     * 得到长连接
     *
     * @return channel
     */
    public abstract AbstractChannel getChannel();

    /**
     * 当前请求数
     *
     * @return 当前请求数 int
     */
    public abstract int currentRequests();

    /**
     * 异步调用
     *
     * @param message 消息
     * @param timeout 超时时间
     * @return 异步Future response future
     * @throws RpcException RpcException
     */
    public abstract ResponseFuture asyncSend(RpcRequest message, int timeout) throws RpcException;

    /**
     * 同步调用
     *
     * @param message 消息
     * @param timeout 超时时间
     * @return RpcRequest base message
     * @throws RpcException RpcException
     */
    public abstract RpcResponse syncSend(RpcRequest message, int timeout) throws RpcException;

    /**
     * 单向调用
     *
     * @param message 消息
     * @param timeout 超时时间
     * @throws RpcException RpcException
     */
    public abstract void oneWaySend(RpcRequest message, int timeout) throws RpcException;

    /**
     * 客户端收到异步响应
     *
     * @param response the response
     */
    public abstract void receiveRpcResponse(RpcResponse response);

    /**
     * 客户端收到服务端的请求，可能是服务端Callback
     *
     * @param request the request
     */
    public abstract void handleRpcRequest(RpcRequest request);

    /**
     * 远程地址
     *
     * @return 远程地址，一般是服务端地址
     */
    public abstract InetSocketAddress remoteAddress();

    /**
     * 本地地址
     *
     * @return 本地地址，一般是客户端地址
     */
    public abstract InetSocketAddress localAddress();

}

