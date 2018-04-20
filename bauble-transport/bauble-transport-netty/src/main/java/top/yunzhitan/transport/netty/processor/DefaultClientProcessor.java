package top.yunzhitan.transport.netty.processor;

import io.netty.channel.Channel;
import top.yunzhitan.rpc.ClientTask;
import top.yunzhitan.transport.ResponseMessage;

import java.util.concurrent.ExecutorService;

public class DefaultClientProcessor implements ClientProcessor {

    private ExecutorService executor;

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
