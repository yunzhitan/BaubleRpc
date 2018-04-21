package top.yunzhitan.transport;

public class ResponseMessage{

    private long invokeId;
    private Status status;
    private byte serializerCode;
    private byte[] bytes;

    public ResponseMessage() {
    }

    public ResponseMessage(long invokeId, Status status) {
        this.invokeId = invokeId;
        this.status = status;
    }

    public ResponseMessage(long invokeId) {
        this.invokeId = invokeId;
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

    public long getInvokeId() {
        return invokeId;
    }

    public void setInvokeId(long invokeId) {
        this.invokeId = invokeId;
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

