package top.yunzhitan.rpc.model;

import top.yunzhitan.transport.RequestMessage;
import top.yunzhitan.transport.ResponseMessage;
import top.yunzhitan.transport.Status;

import java.io.Serializable;

public class RpcResponse implements Serializable{

    private static final long serialVersionUID = 38472457357437534L;
    private ResponseMessage responseMessage;
    private ResponseWrapper wrapper;

    public RpcResponse(long id) {
        responseMessage = new ResponseMessage(id);
    }

    public RpcResponse(ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }


    public RpcResponse(ResponseMessage responseMessage, ResponseWrapper wrapper) {
        this.responseMessage = responseMessage;
        this.wrapper = wrapper;
    }

    public long getId() {
        return responseMessage.getResponseId();
    }

    public byte getStatus() {
        return responseMessage.getStatus();
    }

    public void setStatus(byte status) {
        responseMessage.setStatus(status);
    }

    public void setStatus(Status status) {
        responseMessage.setStatus(status.value());
    }

    public byte getSerializerCode() {
        return responseMessage.serializerCode();
    }

    public void bytes(byte serializerCode, byte[] bytes) {
        responseMessage.bytes(serializerCode, bytes);
    }




    public ResponseMessage getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage (ResponseMessage responseMessage) {
        this.responseMessage = responseMessage;
    }

    public ResponseWrapper getWrapper() {
        return wrapper;
    }

    public void setWrapper(ResponseWrapper wrapper) {
        this.wrapper = wrapper;
    }
}
