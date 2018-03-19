package top.yunzhitan.rpc;
import io.netty.channel.Channel;
import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.rpc.model.RpcResponse;

/**
 * Consumer's hook.
 *
 * 客户端的钩子函数.
 *
 * 在请求发送时触发 {@link #before(RpcRequest, Channel)} 方法;
 * 在响应回来时触发 {@link #after(RpcResponse, Channel)} 方法.
 *
 * jupiter
 * org.jupiter.rpc
 *
 * @author jiachun.fjc
 */
public interface ConsumerHook {

    ConsumerHook[] EMPTY_HOOKS = new ConsumerHook[0];

    /**
     * Triggered when the request data sent to the network.
     */
    void before(RpcRequest request, Channel channel);

    /**
     * Triggered when the server returns the result.
     */
    void after(RpcResponse response, Channel channel);
}
