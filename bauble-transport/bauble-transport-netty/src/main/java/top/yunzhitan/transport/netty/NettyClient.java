package top.yunzhitan.transport.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.AbstractChannelPoolMap;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.Util.SystemPropertyUtil;
import top.yunzhitan.serialization.RpcProtocal;
import top.yunzhitan.transport.Client;
import top.yunzhitan.transport.ConnectionFuture;
import top.yunzhitan.transport.Directory;
import top.yunzhitan.transport.netty.handler.ChannelPoolHander;
import top.yunzhitan.transport.netty.handler.ClientHandler;
import top.yunzhitan.transport.netty.handler.ProtocolDecoder;
import top.yunzhitan.transport.netty.handler.ProtocolEncoder;
import top.yunzhitan.transport.processor.ClientProcessor;

import java.net.SocketAddress;
import java.util.Collection;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class NettyClient implements Client{

    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    /**
     * 序列化方式
     */
    private RpcProtocal protocal;
    private Bootstrap bootstrap;
    private EventLoopGroup workerGroup;
    private int nWorkers = SystemPropertyUtil.AVAILABLE_PROCESSORS;
    private ChannelPoolMap<SocketAddress,ChannelPool> channelPoolMap;
    private ClientProcessor processor;
    private ClientHandler clientHandler = new ClientHandler();

    public NettyClient(RpcProtocal protocal, ClientProcessor processor) {
        this.protocal = protocal;
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
                return new FixedChannelPool(bootstrap.remoteAddress(address),new ChannelPoolHander(),5);
            }
        };

    }
   @Override
    public ConnectionFuture connect(SocketAddress address) {
        return connect(address,false);
    }

    @Override
    public ConnectionFuture connect(SocketAddress address, boolean async) {
        Bootstrap bootstrap = new Bootstrap();
        ThreadFactory workerFactory = new DefaultThreadFactory("bauble-worker",Thread.MAX_PRIORITY);
        workerGroup = new NioEventLoopGroup(nWorkers,workerFactory);
        bootstrap.group(workerGroup).channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline ch = socketChannel.pipeline();
                        ch.addLast(new IdleStateHandler(60,0,0, TimeUnit.SECONDS));
                        ch.addLast(new ProtocolEncoder());
                        ch.addLast(new ProtocolDecoder());
                        ch.addLast(clientHandler);
                    }
                })
                .option(ChannelOption.TCP_NODELAY,true)
                .option(ChannelOption.SO_KEEPALIVE,true);
        ChannelFuture future;
        try {
            future = bootstrap.connect(address).addListener(new ConnectionListener(this));
            if (!async)
                future.sync();
        } catch (Throwable t) {

        }
        return new ConnectionFuture(address,future);
    }

    @Override
    public DefaultChannelGroup getGroup(SocketAddress address) {
        return null;
    }

    @Override
    public Collection<DefaultChannelGroup> getGroups() {
        return null;
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
    public boolean addChannelGroup(Directory directory, DefaultChannelGroup group) {
        return false;
    }

    @Override
    public boolean removeChannelGroup(Directory directory, DefaultChannelGroup group) {
        return false;
    }

    @Override
    public CopyOnWriteGroupList directory(Directory directory) {
        return null;
    }

    @Override
    public boolean isDirectoryAvailable(Directory directory) {
        return false;
    }

    @Override
    public DirectoryJChannelGroup directoryGroup() {
        return null;
    }

    @Override
    public JConnectionManager connectionManager() {
        return null;
    }

    @Override
    public void shutdownGracefully() {

    }
}
