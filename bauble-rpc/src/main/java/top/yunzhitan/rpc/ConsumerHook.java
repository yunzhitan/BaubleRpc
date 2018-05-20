package top.yunzhitan.rpc;
import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.rpc.model.RpcResponse;
import top.yunzhitan.transport.RemotePeer;

/**
 * Consumer's hook.
 *
 * 客户端的钩子函数.
 *
 * 在请求发送时触发 {@link #before(RpcRequest, RemotePeer)} 方法;
 * 在响应回来时触发 {@link #after(RpcResponse, RemotePeer)} 方法.
 *
 */
public interface ConsumerHook {

    ConsumerHook[] EMPTY_HOOKS = new ConsumerHook[0];

    /**
     * Triggered when the request data sent to the network.
     */
    void before(RpcRequest request, RemotePeer remotePeer);

    /**
     * Triggered when the server returns the result.
     */
    void after(RpcResponse response, RemotePeer remotePeer);
}

