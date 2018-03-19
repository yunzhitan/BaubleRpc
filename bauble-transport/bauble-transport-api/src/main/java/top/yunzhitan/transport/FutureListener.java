package top.yunzhitan.transport;

public interface FutureListener<T> {
    void operationSuccess(T c) throws Exception;

    void operationFailure(T c, Throwable cause) throws Exception;

}
