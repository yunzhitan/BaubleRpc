package top.yunzhitan.transport.processor;

import io.netty.channel.Channel;
import top.yunzhitan.transport.RequestMessage;

public interface ServerProcessor {

    /**
     * 由RequestProcessor线程池处理request请求
     * @param channel
     * @param requestMessage request的RequestId
     */
    void handleRequest(Channel channel, RequestMessage requestMessage);


    /**
     * 关闭
     */
    void shutdown();
}
