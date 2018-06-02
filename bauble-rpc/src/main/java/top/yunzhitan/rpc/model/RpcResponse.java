package top.yunzhitan.rpc.model;

import lombok.Data;
import top.yunzhitan.transport.Status;

import java.io.Serializable;


public class RpcResponse implements Serializable{

    private static final long serialVersionUID = -1126932930252953428L;

    private ResultWrapper result;

    private Status status;

    private Long invokeId;

    public RpcResponse(Long invokeId) {
        this.invokeId = invokeId;
    }

    public RpcResponse() {
    }

    public Object getResult() {
        return result.getResult();
    }

    public void setResult(ResultWrapper result) {
        this.result = result;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getInvokeId() {
        return invokeId;
    }

    public void setInvokeId(Long invokeId) {
        this.invokeId = invokeId;
    }

    @Override
    public String toString() {
        return "ResultWrapper{" +
                "result=" + result +
                '}';
    }

}
