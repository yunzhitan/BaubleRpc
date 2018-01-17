package com.yunzhitan.server;

import com.yunzhitan.Util.NetsUtils;
import com.yunzhitan.model.RpcRequest;
import com.yunzhitan.model.RpcResponse;
import com.yunzhitan.protocol.RpcProtocal;
import com.yunzhitan.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;

public class RpcServer {

    private static Logger logger = LoggerFactory.getLogger(RpcServer.class);
    private static volatile ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 4, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(65536));
    private RpcProtocal protocal;
    private String serverAddress;
    private ServerBootstrap bootstrap;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel channel;

    private ServiceRegistry serviceRegistry;
    private static Map<String, Object> serviceBeanMap = new ConcurrentHashMap<>();

    private static RpcServer singleton = new RpcServer();

    public static RpcServer getInstance() {
        return singleton;
    }

    public void setProtocal(RpcProtocal protocal) {
        this.protocal = protocal;
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public Map<String, Object> getServiceBeanMap() {
        return serviceBeanMap;
    }

    public void setServerAddress(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public void stop() {
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }

    private Map<String, Channel> channels;


    public void start() throws Exception{
        bossGroup = new NioEventLoopGroup(4);
        workerGroup = new NioEventLoopGroup();

        try {
            bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup,workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new RpcProviderInitializer(protocal))
                     .option(ChannelOption.SO_BACKLOG,128)
                    .childOption(ChannelOption.SO_KEEPALIVE,true);

            ChannelFuture channelFuture  = bootstrap.bind(NetsUtils.toAddress(serverAddress)).sync();
            logger.info("RpcServer start! bind to the address {}",serverAddress);
            channelFuture.syncUninterruptibly();
            if(serviceRegistry != null) {
                serviceRegistry.register(serverAddress);
            }
            channel = channelFuture.channel();

            channel.closeFuture().sync();
        }catch (Exception e) {
            logger.error("",e);
        }
        finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private Future<?> submit(Callable task){
        Future<?> future =  threadPoolExecutor.submit(task);
        return future;
    }

    public void handleRequest(final ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        Object result = singleton.submit(() -> {
            Object object = null;
            try {
                String className = request.getClassName();
                Object serviceBean = serviceBeanMap.get(className);
                Class<?> serviceClass = serviceBean.getClass();
                String methodName = request.getMethodName();
                Class<?>[] parameterTypes = request.getParameterTypes();
                Object[] parameters = request.getParameters();

                FastClass fastClass = FastClass.create(serviceClass);
                FastMethod fastMethod = fastClass.getMethod(methodName,parameterTypes);
                object =  fastMethod.invoke(serviceBean,parameters);
            } catch (Exception e) {
                logger.error("what happened????",e);
            }
            return object;
        }).get();
        RpcResponse response = new RpcResponse(request.getRequestId(),result);

        ctx.writeAndFlush(response).addListener( (ChannelFutureListener)
                (ChannelFuture) -> {
                    logger.info("send response for requestId {}",request.getRequestId());
                });
    }

}
