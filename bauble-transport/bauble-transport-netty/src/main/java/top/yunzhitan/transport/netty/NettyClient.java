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
import top.yunzhitan.rpc.ConnectionManager;
import top.yunzhitan.rpc.model.Service;
import top.yunzhitan.transport.*;
import top.yunzhitan.transport.netty.handler.ClientHandler;
import top.yunzhitan.transport.netty.handler.NettyChannelPoolHandler;
import top.yunzhitan.transport.processor.ClientProcessor;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;

public class NettyClient implements Client{

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    /**
     * 序列化方式
     */
    private ConcurrentMap<SocketAddress,RemotePeer> remotepeerMap = new ConcurrentHashMap<>();
    private RemotePeerManager peerManager = new RemotePeerManager();
    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;
    private int nWorkers = SystemPropertyUtil.AVAILABLE_PROCESSORS;
    private AbstractChannelPoolMap<SocketAddress,ChannelPool> channelPoolMap;
    private ClientProcessor processor;
    private ConnectionManager manager;
    private ClientHandler clientHandler = new ClientHandler();

    public NettyClient(ClientProcessor processor) {
        this.processor = processor;
    }

    /**
     * Client实例化后必须先调用init方法进行初始化
     */
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

    }

    public void subscribe(Service service) {

    }

   @Override
    public ConnectionFuture writeMessage(SocketAddress address, RequestMessage message, top.yunzhitan.transport.FutureListener listener) {
        return writeMessage(address,message,false,listener);
    }


    @Override
    public ConnectionFuture writeMessage(SocketAddress address, RequestMessage message, boolean async, top.yunzhitan.transport.FutureListener listener) {
        ChannelPool channelPool = channelPoolMap.get(address);
        if(channelPool != null) {
            Future<Channel> future = channelPool.acquire();
            future.addListener(new FutureListener<Channel>() {
                @Override
                public void operationComplete(Future<Channel> channelFuture) {
                    if(channelFuture.isSuccess()) {
                        Channel channel = future.getNow();
                        channel.writeAndFlush(message).addListener(new ChannelFutureListener() {
                            @Override
                            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                                if(channelFuture.isSuccess()) {
                                    listener.operationSuccess(channel);
                                }
                                else {
                                    listener.operationFailure(channel,channelFuture.cause());
                                }
                            }
                        });
                        channelPool.release(channel);
                    }
                }
            });
        }
    }

    @Override
    public RemotePeer getRemotePeer(SocketAddress address) {
        RemotePeer remotePeer = remotepeerMap.computeIfAbsent(address,
                k->new RemotePeer(address));
        return remotePeer;
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
    public boolean addRemotePeer(Directory directory, RemotePeer remotePeer) {
        CopyOnWriteArrayList<RemotePeer> peerLists = peerManager.find(directory);
        boolean success = peerLists.addIfAbsent(remotePeer);
        if(success) {
            peerManager.incrementRefCount(remotePeer);
            logger.info("Added RemotePeer {} to {}",remotePeer,directory.directory());
        }
        return success;
    }

    @Override
    public boolean removeRemotePeer(Directory directory,RemotePeer remotePeer) {
        CopyOnWriteArrayList<RemotePeer> peerList = peerManager.find(directory);
        boolean remove = peerList.remove(remotePeer);
        if(remove) {
            peerManager.decrementRefCount(remotePeer);
            logger.warn("Remove RemotePeer {} to {}",remotePeer,directory.directory());
        }
        return remove;
    }

    @Override
    public boolean isDirectoryAvailable(Directory directory) {
        CopyOnWriteArrayList<RemotePeer> peerList = peerManager.find(directory);
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
        future.addListener( (channelFuture)-> {
            if(channelFuture.isSuccess()) {
                Channel channel = (Channel)channelFuture.getNow();
                listener.operationSuccess(channel);
                channelPool.release(channel);
            }
            else {
                listener.operationFailure(channelFuture.cause());
            }
        });

    @Override
    public void shutdownGracefully() {
        workerGroup.shutdownGracefully();
    }
}
