package top.yunzhitan.transport.netty.processor;

import io.netty.channel.Channel;
import top.yunzhitan.transport.RequestMessage;
import top.yunzhitan.transport.Status;

public interface ServerProcessor {

    /**
     * 由RequestProcessor线程池处理request请求
     * @param channel
     * @param requestMessage request的RequestId
     */
    void handleRequest(Channel channel, RequestMessage requestMessage);


    /**
     * 处理异常
     * @param channel
     * @param requestMessage
     * @param status
     * @param t
     */
    void handleException(Channel channel, RequestMessage requestMessage, Status status, Throwable t);

    /**
     * 关闭
     */
    void shutdown();
}
