package top.yunzhitan.transport.processor;

import io.netty.channel.Channel;
import top.yunzhitan.rpc.ClientTask;
import top.yunzhitan.transport.ResponseMessage;
import top.yunzhitan.transport.processor.ClientProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultClientProcessor implements ClientProcessor {

    private ExecutorService executor = Executors.newFixedThreadPool(5);


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
