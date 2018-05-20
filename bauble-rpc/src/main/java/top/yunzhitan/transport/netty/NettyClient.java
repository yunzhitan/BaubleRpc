package top.yunzhitan.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.Util.SystemPropertyUtil;
import top.yunzhitan.registry.RegistryConfig;
import top.yunzhitan.rpc.ConnectionManager;
import top.yunzhitan.common.Service;
import top.yunzhitan.rpc.DefaultConnectionManager;
import top.yunzhitan.transport.*;
import top.yunzhitan.transport.netty.handler.NettyChannelPoolHandler;
import top.yunzhitan.transport.processor.ClientProcessor;
import top.yunzhitan.transport.processor.DefaultClientProcessor;

import java.net.SocketAddress;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("unchecked")
public class NettyClient implements Client{

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    /**
     * 序列化方式
     */
    private RemotePeerManager peerManager = new RemotePeerManager();
    private String appName;
    private Bootstrap bootstrap;
    private Service service;
    private EventLoopGroup workerGroup;
    private int nWorkers = SystemPropertyUtil.AVAILABLE_PROCESSORS;
    private AbstractChannelPoolMap<SocketAddress,ChannelPool> channelPoolMap;
    private ClientProcessor processor = new DefaultClientProcessor();
    private ConnectionManager connectionManager;

    public NettyClient(String appName) {
        this.appName = appName;
    }

    public NettyClient() {
    }

    /**
     * Client实例化后必须先调用init方法进行初始化
     */
    @Override
    public void init() {
        bootstrap = new Bootstrap();
        ThreadFactory workerFactory = new DefaultThreadFactory("bauble-worker",Thread.MAX_PRIORITY);
        workerGroup = new NioEventLoopGroup(nWorkers,workerFactory);
        bootstrap.group(workerGroup).channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.SO_KEEPALIVE,true);

        channelPoolMap = new AbstractChannelPoolMap<SocketAddress, ChannelPool>() {
            @Override
            protected ChannelPool newPool(SocketAddress address) {
                return new FixedChannelPool(bootstrap.remoteAddress(address),new NettyChannelPoolHandler(processor),5);
            }
        };

        connectionManager = new DefaultConnectionManager(appName,this);
    }

    public void subscribe(Service service) {

    }

   @Override
    public void writeMessage(SocketAddress address, RequestMessage message, top.yunzhitan.transport.FutureListener listener) {
        writeMessage(address,message,false,listener);
    }


    @Override
    public void writeMessage(SocketAddress address, RequestMessage message, boolean async, top.yunzhitan.transport.FutureListener listener) {
        ChannelPool channelPool = channelPoolMap.get(address);
        if(channelPool != null) {
            Future<Channel> future = channelPool.acquire();
            future.addListener((FutureListener<Channel>) channelFuture -> {
                if(channelFuture.isSuccess()) {
                    Channel channel = future.getNow();
                    channel.writeAndFlush(message).addListener((ChannelFutureListener) channelFuture1 -> {
                        if (channelFuture1.isSuccess()) {
                            logger.info("Send Request Succeed on Chanel:{}", channel);
                            listener.operationSuccess(channel);
                        } else {
                            listener.operationFailure(channelFuture1.cause());
                        }
                    });
                    channelPool.release(channel);
                }
            });
        }
    }

    @Override
    public RemotePeer getRemotePeer(RegistryConfig registryConfig) {
        return peerManager.findRemotePeer(registryConfig);
    }
    @Override
    public ClientProcessor getProcessor() {
        return processor;
    }

    @Override
    public void setProcessor(ClientProcessor processor) {
        this.processor = processor;
    }

    @Override
    public boolean addRemotePeer(Service service, RemotePeer remotePeer) {
        CopyOnWriteArrayList<RemotePeer> peerLists = peerManager.find(service);
        boolean success = peerLists.addIfAbsent(remotePeer);
        if(success) {
            peerManager.incrementRefCount(remotePeer);
            logger.info("Added RemotePeer {} to {}",remotePeer,service.getDirectory());
        }
        return success;
    }

    @Override
    public boolean removeRemotePeer(Service service,RemotePeer remotePeer) {
        CopyOnWriteArrayList<RemotePeer> peerList = peerManager.find(service);
        boolean remove = peerList.remove(remotePeer);
        if(remove) {
            peerManager.decrementRefCount(remotePeer);
            logger.warn("Remove RemotePeer {} to {}",remotePeer,service.getDirectory());
        }
        return remove;
    }



    @Override
    public boolean isServiceAvalible(Service service) {
        CopyOnWriteArrayList<RemotePeer> peerList = peerManager.find(service);
        for(RemotePeer peer : peerList) {
            if(peer.isAvailable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void tryConnect(SocketAddress address, top.yunzhitan.transport.FutureListener listener) {
        ChannelPool channelPool = channelPoolMap.get(address);
        Future<Channel> future = channelPool.acquire(); //acquire时自动建立Channel连接
        future.addListener((channelFuture) -> {
            if (channelFuture.isSuccess()) {
                Channel channel = (Channel) channelFuture.getNow();
                listener.operationSuccess(channel);
                channelPool.release(channel);
            } else {
                listener.operationFailure(channelFuture.cause());
            }
        });
    }

    @Override
    public CopyOnWriteArrayList<RemotePeer> getRemotePeerList(Service service) {
        return peerManager.find(service);
    }

    @Override
    public void shutdownGracefully() {
        workerGroup.shutdownGracefully();
        connectionManager.shutdownGracefully();
    }

    @Override
    public String getAppName() {
        return appName;
    }

    @Override
    public void connectRegistryServer(String registryConfig) {
        connectionManager.connectRegistryServer(registryConfig);
    }

    @Override
    public void setServiceConsumer(Class<?> interfaceClass, String version) {
        top.yunzhitan.rpc.Service annotation = interfaceClass.getAnnotation(top.yunzhitan.rpc.Service.class);
        checkNotNull(annotation, interfaceClass + " is not a Service interface");
        String serviceName = annotation.name();
        String group = annotation.group();
        service = new Service(group,serviceName,version);
        connectionManager.initialization(service);
    }

    @Override
    public boolean waitForAvailable(long timeoutMillis) {
        return connectionManager.waitForAvailable(timeoutMillis,service);
    }
}
