package top.yunzhitan.transport.netty.processor;

import io.netty.channel.Channel;
import top.yunzhitan.transport.RequestMessage;
import top.yunzhitan.transport.ResponseMessage;
import top.yunzhitan.transport.Status;

public interface ClientProcessor {

    /**
     * 由ClientProcessor线程池处理response请求
     * @param channel
     * @param responseMessage
     */
    void handleResponse(Channel channel, ResponseMessage responseMessage);


    /**
     * 关闭
     */
    void shutdown();
}
