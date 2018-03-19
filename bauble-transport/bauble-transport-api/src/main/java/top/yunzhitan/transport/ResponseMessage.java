package top.yunzhitan.transport;

import top.yunzhitan.transport.BytesHolder;

public class ResponseMessage extends BytesHolder{

    private long responseId;
    private byte status;

    public ResponseMessage(long responseId, byte status) {
        this.responseId = responseId;
        this.status = status;
    }

    public ResponseMessage(long responseId) {
        this.responseId = responseId;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public long getResponseId() {
        return responseId;
    }

    public void setResponseId(long responseId) {
        this.responseId = responseId;
    }

    public byte serializerCode() {
        return serializerCode;
    }
}

