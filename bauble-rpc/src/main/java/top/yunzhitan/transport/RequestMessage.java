package top.yunzhitan.transport;


import lombok.Data;
import top.yunzhitan.Util.id.IdWorker;

/**
 * requestMessage位于传输层，不关注消息体的结构
 */
@Data
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


    public void nullBytes() {
        bytes = null; // help gc
    }

    public int size() {
        return bytes == null ? 0 : bytes.length;
    }
}
