package top.yunzhitan.transport;

public interface FutureListener<T> {
    void operationSuccess(T c) throws Exception;

    void operationFailure(Throwable cause) throws Exception;

}
