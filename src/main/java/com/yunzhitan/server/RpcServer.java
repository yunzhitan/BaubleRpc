package com.yunzhitan.server;

import com.yunzhitan.Util.AddressUtil;
import com.yunzhitan.model.RpcRequest;
import com.yunzhitan.protocol.RpcProtocal;
import com.yunzhitan.registry.ServiceRegistry;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.*;

import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RpcServer.class);

    private String serverAddress;
    private ServiceRegistry serviceRegistry;
    private RpcProtocal protocal;
    private static Map<String, Object> serviceBeanMap = new ConcurrentHashMap<>();
    private static volatile ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(4, 4, 600L, TimeUnit.SECONDS, new ArrayBlockingQueue<>(65536));


    public RpcServer(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcServer(String serverAddress, ServiceRegistry serviceRegistry, RpcProtocal protocal) {
        this.serverAddress = serverAddress;
        this.serviceRegistry = serviceRegistry;
        this.protocal = protocal;
    }

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        Map<String, Object> serviceTempMap = ctx.getBeansWithAnnotation(RpcService.class);
        if (MapUtils.isNotEmpty(serviceTempMap)) {
            for (Object serviceBean : serviceTempMap.values()) {
                String interfaceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                serviceBeanMap.put(interfaceName, serviceBean);
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .childHandler(new RpcProviderInitializer(protocal))
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            InetSocketAddress socketAddress = AddressUtil.getSocketAddress(serverAddress);
            ChannelFuture future = bootstrap.bind(socketAddress).sync();
            logger.debug("Server started on port {}", socketAddress.getPort());

            if (serviceRegistry != null) {
                serviceRegistry.register(serverAddress);
            }

            future.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    private static Future<?> submit(Callable task){
        Future<?> future =  threadPoolExecutor.submit(task);
        return future;
    }

    public static Object handleRequest(RpcRequest request) throws Exception {
        Object result = RpcServer.submit(() -> {
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
        return result;
    }
}
