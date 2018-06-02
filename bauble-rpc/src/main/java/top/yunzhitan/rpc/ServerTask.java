package top.yunzhitan.rpc;

import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.Util.ThrowUtil;
import top.yunzhitan.rpc.exception.*;
import top.yunzhitan.rpc.model.ResultWrapper;
import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.rpc.provider.Provider;
import top.yunzhitan.rpc.provider.FlowController;
import top.yunzhitan.serialization.Serializer;
import top.yunzhitan.serialization.SerializerFactory;
import top.yunzhitan.transport.*;


public class ServerTask implements Runnable{

    private Channel channel;
    private static final Logger logger = LoggerFactory.getLogger(ServerTask.class);
    private RequestMessage requestMessage;
    private Server server;
    private ResponseMessage responseMessage = new ResponseMessage();

    public ServerTask(Channel channel, RequestMessage requestMessage,Server server) {
        this.channel = channel;
        this.requestMessage = requestMessage;
        this.server = server;
    }

    @Override
    public void run() {
        // stack copy

        RpcRequest request;
        try {
            byte s_code = requestMessage.getSerializerCode();
            Serializer serializer = SerializerFactory.getSerializer(s_code);
            byte[] bytes = requestMessage.getBytes();
            request = serializer.readObject(bytes, RpcRequest.class);
        } catch (Throwable t) {
            rejectedRequest(Status.BAD_REQUEST, new BadRequestException(t.getMessage()));
            return;
        }

        // 查找服务
        final Provider provider = server.findServiceProvider(request.getServiceConfig());
        if (provider == null) {
            rejectedRequest(Status.SERVICE_NOT_FOUND, new ServiceNotFoundException(String.valueOf(request.getServiceConfig())));
            return;
        }

        //provider私有流量控制
        FlowController<RpcRequest> childController = provider.getFlowController();
        if (childController != null) {
            if(!childController.flowControl(request)){
                rejectedRequest(Status.PROVIDER_FLOW_CONTROL, new FlowControlException());
                return;
            }
        }

        responseMessage.setInvokeId(requestMessage.getInvokeId());
        responseMessage.setSerializerCode(requestMessage.getSerializerCode());
        byte s_code = requestMessage.getSerializerCode();
        Serializer serializer = SerializerFactory.getSerializer(s_code);
        ResultWrapper resultWrapper;
        try {
            resultWrapper = provider.invoke(request);
            byte[] bytes = serializer.writeObject(resultWrapper);
            responseMessage.setBytes(bytes);
            responseMessage.setStatus(Status.OK);
            writeResponse(responseMessage);
            logger.info("write Response on Channel:{}",channel);
        } catch (Throwable t) {
            handleException(Status.SERVICE_UNEXPECTED_ERROR,t);
        }

    }

    private void rejectedRequest(Status status, Throwable t) {
        handleException(status,t);
    }

    private void handleException( Status status, Throwable t) {
        ResultWrapper wrapper = new ResultWrapper();
        t = ThrowUtil.cutCause(t);
        wrapper.setResult(t);
        Serializer serializer = SerializerFactory.getSerializer(requestMessage.getSerializerCode());
        byte[] bytes = serializer.writeObject(requestMessage);
        responseMessage.setStatus(status);
        responseMessage.setBytes(bytes);
        writeResponse(responseMessage);
    }

    private void writeResponse(ResponseMessage response) {
        channel.writeAndFlush(response);
    }

}
