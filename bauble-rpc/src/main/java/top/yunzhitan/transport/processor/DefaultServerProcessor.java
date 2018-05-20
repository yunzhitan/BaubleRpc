package top.yunzhitan.transport.processor;

import io.netty.channel.Channel;
import top.yunzhitan.rpc.ServerTask;
import top.yunzhitan.transport.RequestMessage;
import top.yunzhitan.transport.Server;
import top.yunzhitan.transport.processor.ServerProcessor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DefaultServerProcessor implements ServerProcessor {

    private ExecutorService executor = Executors.newFixedThreadPool(5);
    private Server server;

    public DefaultServerProcessor(Server server) {
        this.server = server;
    }

    @Override
    public void handleRequest(Channel channel, RequestMessage requestMessage) {
        ServerTask task = new ServerTask(channel,requestMessage,server);
        executor.execute(task);
    }

    @Override
    public void shutdown() {
        executor.shutdown();
    }
}
