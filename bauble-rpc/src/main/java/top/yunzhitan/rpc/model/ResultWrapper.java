package top.yunzhitan.rpc.model;

import java.io.Serializable;

public class ResultWrapper implements Serializable{

    private static final long serialVersionUID = -4534855430252953428L;


    private Object result; // 响应结果对象, 也可能是异常对象, 由响应状态决定


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }
}
