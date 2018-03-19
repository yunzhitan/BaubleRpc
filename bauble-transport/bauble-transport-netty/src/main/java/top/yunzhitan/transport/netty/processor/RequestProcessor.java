package top.yunzhitan.transport.netty.processor;

import top.yunzhitan.transport.RequestMessage;
import top.yunzhitan.transport.Status;

public interface RequestProcessor {

    /**
     * 由RequestProcessor线程池处理request请求
     * @param channel
     * @param requestMessage request的RequestId
     */
    void handleRequest(io.netty.channel.Channel channel, RequestMessage requestMessage);


    /**
     * 处理异常
     * @param channel
     * @param requestMessage
     * @param status
     * @param t
     */
    void handleException(io.netty.channel.Channel channel, RequestMessage requestMessage, Status status, Throwable t);

    /**
     * 关闭
     */
    void shutdown();
}
