package top.yunzhitan.transport;


import top.yunzhitan.Util.id.IdWorker;

/**
 * requestMessage位于传输层，不关注消息体的结构
 */
public class RequestMessage {
    private long invokeId;
    private long timestamp;
    private byte serializerCode;
    private byte[] bytes;

    public RequestMessage() {
        this(IdWorker.getInstance().nextId());
    }

    public RequestMessage(long invokeId, long timestamp) {
        this.invokeId = invokeId;
        this.timestamp = timestamp;
    }

    public RequestMessage(long invokeId) {
        this.invokeId = invokeId;
    }

    public long getInvokeId() {
        return invokeId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte getSerializerCode() {
        return serializerCode;
    }

    public void setSerializerCode(byte serializerCode) {
        this.serializerCode = serializerCode;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getbytes() {
        return bytes;
    }

    public void nullBytes() {
        bytes = null; // help gc
    }

    public int size() {
        return bytes == null ? 0 : bytes.length;
    }
}
