package top.yunzhitan.transport.netty.processor;

import io.netty.channel.Channel;
import top.yunzhitan.rpc.ClientTask;
import top.yunzhitan.transport.ResponseMessage;
import top.yunzhitan.transport.processor.ClientProcessor;

import java.util.concurrent.ExecutorService;

public class DefaultClientProcessor implements ClientProcessor {

    private ExecutorService executor;

    public DefaultClientProcessor(ExecutorService executor) {
        this.executor = executor;
    }

    @Override
    public void handleResponse(Channel channel, ResponseMessage responseMessage) {
        ClientTask clientTask = new ClientTask(channel,responseMessage);
        if(executor == null) {
            clientTask.run();
        }
        else {
            executor.execute(clientTask);
        }
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
