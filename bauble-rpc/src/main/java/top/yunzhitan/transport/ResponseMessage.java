package top.yunzhitan.transport;

import lombok.Data;

@Data
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

}

