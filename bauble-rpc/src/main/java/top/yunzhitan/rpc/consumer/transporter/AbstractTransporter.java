package top.yunzhitan.rpc.consumer.transporter;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.rpc.consumer.loadbalance.LoadBalancer;
import top.yunzhitan.rpc.exception.RemoteException;
import top.yunzhitan.rpc.future.DefaultInvokeFuture;
import top.yunzhitan.rpc.future.FuturePool;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.ResultWrapper;
import top.yunzhitan.rpc.model.RpcResponse;
import top.yunzhitan.common.ServiceConfig;
import top.yunzhitan.serialization.Serializer;
import top.yunzhitan.transport.*;

import java.util.concurrent.CopyOnWriteArrayList;

public abstract class AbstractTransporter implements Transporter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTransporter.class);

    private Long timeoutMills;           //超时时间
    private Serializer serializer;       //序列化方式
    private Client client;
    private LoadBalancer loadBalancer; //负载均衡方式

    public AbstractTransporter(Serializer serializer, Client client,LoadBalancer loadBalancer) {
        this.serializer = serializer;
        this.client = client;
        this.loadBalancer = loadBalancer;
    }

    protected RemotePeer select(ServiceConfig serviceConfig) {
        CopyOnWriteArrayList<RemotePeer> peerList = client.getRemotePeerList(serviceConfig);
        return loadBalancer.select(peerList);
    }


    @SuppressWarnings("all")
    public <T> InvokeFuture<T> write(RequestMessage request, Class<T> returnType
    , DefaultInvokeFuture future, RemotePeer remotePeer) {

        client.writeMessage(remotePeer.getRemoteAddress(), request, true, new FutureListener<Channel>() {
            @Override
            public void operationSuccess(Channel channel) throws Exception {
                future.markSent();
            }

            @Override
            public void operationFailure(Throwable cause) throws Exception {
                if(logger.isWarnEnabled()) {
                    logger.warn("Write failed due to {}",cause);
                    ResultWrapper wrapper = new ResultWrapper();
                    wrapper.setResult(new RemoteException(cause));

                    RpcResponse response = new RpcResponse(request.getInvokeId());
                    response.setStatus(Status.CLIENT_ERROR);
                    response.setResult(wrapper);

                    FuturePool.receiveResponse(remotePeer,response);
                }
            }
        });
        return future;
    }


    @Override
    public Transporter timeoutMillis(long timeoutMillis) {
        if(timeoutMillis > 0) {
            this.timeoutMills = timeoutMillis;
        }
        return this;
    }

    protected Long getTimeoutMills() {
        return timeoutMills;
    }

    protected Serializer getSerializer() {
        return serializer;
    }

    protected Client getClient() {
        return client;
    }
}
