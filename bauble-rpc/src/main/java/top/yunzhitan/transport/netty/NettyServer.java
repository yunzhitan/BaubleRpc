package top.yunzhitan.transport.netty;

import io.netty.channel.*;
import top.yunzhitan.Util.SystemPropertyUtil;
import top.yunzhitan.registry.RegistryType;
import top.yunzhitan.rpc.DefaultProviderManager;
import top.yunzhitan.rpc.ProviderManager;
import top.yunzhitan.common.ServiceConfig;
import top.yunzhitan.rpc.provider.Provider;
import top.yunzhitan.transport.Server;
import top.yunzhitan.transport.netty.handler.ProtocolDecoder;
import top.yunzhitan.transport.netty.handler.ProtocolEncoder;
import top.yunzhitan.transport.netty.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.transport.processor.DefaultServerProcessor;
import top.yunzhitan.transport.processor.ServerProcessor;

import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class NettyServer implements Server {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private ServerBootstrap serverBootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private SocketAddress localAddress;
    private ServerProcessor processor = new DefaultServerProcessor(this);
    private ServerHandler handler = new ServerHandler(processor);

    private ProviderManager providerManager;
    private RegistryType registryType;

    private int nBosses = 1;
    private int nWorkers = SystemPropertyUtil.AVAILABLE_PROCESSORS << 1;

    public NettyServer(SocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    public NettyServer() {
    }


    @Override
    public void start() throws InterruptedException{
            ThreadFactory bossFactory = new DefaultThreadFactory("bauble-boss",Thread.MAX_PRIORITY);
            ThreadFactory workerFactory = new DefaultThreadFactory("bauble-worker",Thread.MAX_PRIORITY);
            bossGroup = new NioEventLoopGroup(nBosses,bossFactory);
            workerGroup = new NioEventLoopGroup(nWorkers,workerFactory);
            serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup,workerGroup)
                            .channel(NioServerSocketChannel.class)
                            .childHandler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel ch) {
                                    ch.pipeline().addLast(
                                            new IdleStateHandler(60,0,0, TimeUnit.SECONDS),
                                            new ProtocolDecoder(),
                                            new ProtocolEncoder(),
                                            handler
                                    );
                                }
                            })
                            .option(ChannelOption.SO_BACKLOG,128)
                            .option(ChannelOption.SO_REUSEADDR,true)
                            .option(ChannelOption.SO_RCVBUF,256*1024)
                            .childOption(ChannelOption.SO_REUSEADDR, true)
                            .childOption(ChannelOption.SO_RCVBUF, 256 * 1024)
                            .childOption(ChannelOption.SO_SNDBUF, 256 * 1024)
                            .childOption(ChannelOption.WRITE_BUFFER_WATER_MARK, //
                            new WriteBufferWaterMark(1024 * 1024, 2048 * 1024));

        ChannelFuture cf = serverBootstrap.bind(localAddress).sync();
            logger.info("BaubleServer start at {}",localAddress);
            cf.channel().closeFuture().sync();
    }

    @Override
    public void shutdownGracefully() {
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        providerManager.shutdownGracefully();
        if(processor != null) {
            processor.shutdown();
        }
        logger.info("Server shutdownGracefully");
    }

    @Override
    public SocketAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public void setLocalAddress(SocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    @Override
    public void setProcessor(ServerProcessor processor) {
        this.processor = processor;
    }

    @Override
    public ServerProcessor getRequestProcessor() {
        return processor;
    }

    @Override
    public Provider findServiceProvider(ServiceConfig serviceConfig) {
        return providerManager.findService(serviceConfig);
    }

    @Override
    public void publish(Provider provider) {
        providerManager.publish(provider);
    }

    @Override
    public void unpublish(Provider provider) {
        providerManager.unpublish(provider);
    }

    @Override
    public void init() {
        if(localAddress == null) {
            logger.error("Not assign localAddress!!");
            throw new RuntimeException();
        }
         providerManager = new DefaultProviderManager(localAddress,registryType);
    }

    @Override
    public void setRegistryType(RegistryType registryType) {
        this.registryType = registryType;
    }

    @Override
    public RegistryType getRegistryType() {
        return registryType;
    }

    @Override
    public void connectRegistryServer(String registryConfig) {
        providerManager.connectRegistryServer(registryConfig);
    }

}
