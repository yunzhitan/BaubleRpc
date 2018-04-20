package top.yunzhitan.transport;

import top.yunzhitan.transport.BytesHolder;

public class ResponseMessage{

    private long responseId;
    private Status status;
    protected byte serializerCode;
    protected byte[] bytes;

    public ResponseMessage() {
    }

    public ResponseMessage(long responseId, Status status) {
        this.responseId = responseId;
        this.status = status;
    }

    public ResponseMessage(long responseId) {
        this.responseId = responseId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = Status.parse(status);
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getResponseId() {
        return responseId;
    }

    public void setResponseId(long responseId) {
        this.responseId = responseId;
    }

    public byte getSerializerCode() {
        return serializerCode;
    }

    public void setSerializerCode(byte serializerCode) {
        this.serializerCode = serializerCode;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}

