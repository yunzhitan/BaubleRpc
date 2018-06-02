package top.yunzhitan.rpc.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultWrapper implements Serializable{

    private static final long serialVersionUID = -4534855430252953428L;


    private Object result; // 响应结果对象, 也可能是异常对象, 由响应状态决定

}
