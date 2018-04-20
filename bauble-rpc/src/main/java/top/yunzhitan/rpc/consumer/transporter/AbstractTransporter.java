package top.yunzhitan.rpc.consumer.transporter;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.rpc.ConsumerHook;
import top.yunzhitan.rpc.exception.RemoteException;
import top.yunzhitan.rpc.future.DefaultInvokeFuture;
import top.yunzhitan.rpc.future.InvokeFuture;
import top.yunzhitan.rpc.model.MethodSpecialConfig;
import top.yunzhitan.rpc.model.ResultWrapper;
import top.yunzhitan.rpc.model.RpcResponse;
import top.yunzhitan.serialization.Serializer;
import top.yunzhitan.transport.Client;
import top.yunzhitan.transport.FutureListener;
import top.yunzhitan.transport.RequestMessage;
import top.yunzhitan.transport.Status;

import java.net.SocketAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractTransporter implements Transporter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractTransporter.class);

    private Long timeoutMills;           //超时时间
    private Serializer serializer;       //序列化方式
    private Client client;

    public AbstractTransporter(Serializer serializer, Client client) {
        this.serializer = serializer;
        this.client = client;
    }

    private Map<String,Long> methodSpecialTimeoutMapping = new HashMap<>();



    @SuppressWarnings("all")
    public <T> InvokeFuture<T> write(RequestMessage request, Class<T> returnType
    , DefaultInvokeFuture future, SocketAddress address) {
        client.writeMessage(address, request, true, new FutureListener<Channel>() {
            @Override
            public void operationSuccess(Channel channel) throws Exception {
                future.markSent();
            }

            @Override
            public void operationFailure(Throwable cause) throws Exception {
                if(logger.isWarnEnabled()) {
                    logger.warn("Write to {} failed due to {}",channel,cause);
                    ResultWrapper wrapper = new ResultWrapper();
                    wrapper.setResult(new RemoteException(cause));

                    RpcResponse response = new RpcResponse(request.getInvokeId());
                    response.setStatus(Status.CLIENT_ERROR);
                    response.setResult(wrapper);

                    DefaultInvokeFuture.receiveResponse(channel,response);
                }
            }
        });
        return future;
    }

    @Override
    public Transporter hooks(List<ConsumerHook> hooks) {
        return null;
    }

    @Override
    public Transporter timeoutMillis(long timeoutMillis) {
        if(timeoutMillis > 0) {
            this.timeoutMills = timeoutMillis;
        }
        return this;
    }

    @Override
    public Transporter methodSpecialConfigs(List<MethodSpecialConfig> methodSpecialConfigs) {
        if(!methodSpecialConfigs.isEmpty()) {
            for(MethodSpecialConfig specialConfig : methodSpecialConfigs) {
                String methodName = specialConfig.getMethodName();
                long timeoutMillis = specialConfig.getTimeoutMillis();
                if(timeoutMillis > 0) {
                    methodSpecialTimeoutMapping.put(methodName,timeoutMillis);
                }
            }
        }
        return this;
    }

    public Long getTimeoutMills() {
        return timeoutMills;
    }

    public Serializer getSerializer() {
        return serializer;
    }

    public Client getClient() {
        return client;
    }
}
