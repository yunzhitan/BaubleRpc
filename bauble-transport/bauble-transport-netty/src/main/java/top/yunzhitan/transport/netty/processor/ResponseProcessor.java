package top.yunzhitan.transport.netty.processor;


import io.netty.channel.Channel;
import top.yunzhitan.transport.RequestMessage;
import top.yunzhitan.transport.Status;

/**
 * Consumer的Response处理类
 */
public interface ResponseProcessor {

    /**
     * 处理Response
     * @param channel
     * @param requestMessage
     * @throws Exception
     */
    void handleResponse(Channel channel, RequestMessage requestMessage) throws Exception;

    /**
     * 处理异常
     * @param channel
     * @param requestMessage
     * @param status
     * @param t
     */
    void handleException(Channel channel, RequestMessage requestMessage, Status status, Throwable t);
}
