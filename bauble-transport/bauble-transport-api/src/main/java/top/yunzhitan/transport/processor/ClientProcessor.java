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

    void shutdown();

}
