package top.yunzhitan.rpc.exception;

public class RpcException extends RuntimeException{

    public RpcException() {
    }

    public RpcException(String message) {
        super(message);
    }

}
