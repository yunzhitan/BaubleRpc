package top.yunzhitan.transport;

import top.yunzhitan.rpc.id.IdWorker;

/**
 * requestMessage位于传输层，不关注消息体的结构
 */
public class RequestMessage extends BytesHolder{
    private long requestId;
    private long timestamp;

    public RequestMessage() {
        this(IdWorker.getInstance().nextId());
    }

    public RequestMessage(long requestId, long timestamp) {
        this.requestId = requestId;
        this.timestamp = timestamp;
    }

    public RequestMessage(long requestId) {
        this.requestId = requestId;
    }

    public long getRequestId() {
        return requestId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public byte serializerCode() {
        return serializerCode;
    }
}
