package top.yunzhitan.transport.processor;

import top.yunzhitan.transport.ResponseMessage;
import top.yunzhitan.transport.Status;
import io.netty.channel.Channel;

/**
 * Consumer的Response处理类
 */
public interface ClientProcessor {

    /**
     * 处理Response
     * @param channel
     * @param requestId
     * @throws Exception
     */
    void handleResponse(Channel channel, ResponseMessage requestId) throws Exception;

    /**
     * 处理异常
     * @param channel
     * @param requestId
     * @param status
     * @param t
     */
    void handleException(Channel channel, ResponseMessage requestId, Status status, Throwable t);
}
