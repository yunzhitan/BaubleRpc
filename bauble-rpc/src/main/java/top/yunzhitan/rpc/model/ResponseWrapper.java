package top.yunzhitan.rpc.model;

import java.io.Serializable;

public class ResponseWrapper implements Serializable{
    private static final long serialVersionUID = -1126932930252953428L;

    private Object result; // 响应结果对象, 也可能是异常对象, 由响应状态决定

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public void setError(Throwable cause) {
        result = cause;
    }

    @Override
    public String toString() {
        return "ResultWrapper{" +
                "result=" + result +
                '}';
    }

}
