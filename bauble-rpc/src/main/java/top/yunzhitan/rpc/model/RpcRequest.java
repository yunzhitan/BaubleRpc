package top.yunzhitan.rpc.model;

import top.yunzhitan.transport.RequestMessage;
import top.yunzhitan.rpc.tracing.TraceId;

import java.io.Serializable;

public class RpcRequest implements Serializable{

    private static final long serialVersionUID = -47365345647583434L;

    private RequestMessage requestMessage;
    private RequestWrapper requestWrapper;

    public RpcRequest() {
        this.requestMessage = new RequestMessage();
    }

    public RequestWrapper getRequestWrapper() {
        return requestWrapper;
    }

    public void setRequestWrapper(RequestWrapper requestWrapper) {
        this.requestWrapper = requestWrapper;
    }

    public RequestMessage getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(RequestMessage requestMessage) {
        this.requestMessage = requestMessage;
    }


    @Override
    public String toString() {
        return "RpcRequest{" +
                "requestWrapper=" + requestWrapper +
                '}';
    }
}