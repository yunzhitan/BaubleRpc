package com.yunzhitan.client;

import com.yunzhitan.model.RpcRequest;
import com.yunzhitan.protocol.RpcProtocal;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolMap;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectManageTemp {
    private final Logger logger = LoggerFactory.getLogger(ConnectManage.class);
    private volatile static ConnectManageTemp singleton;
    private RpcProtocal protocal;
    private Bootstrap bootstrap;
    private EventLoopGroup loopGroup;
    private ChannelPool channelPool;
    private AtomicInteger roundRobin = new AtomicInteger(0);

    private Map<InetSocketAddress,Channel> channelMap = new ConcurrentHashMap<>();
    private Map<String,RPCFuture> pendingRpcs = new ConcurrentHashMap<>();

    private volatile Boolean isRunning = true;

    private ConnectManageTemp() {

    }

    public static ConnectManageTemp getInstance() {
        if(singleton == null) {
            synchronized (ConnectManageTemp.class) {
                if (singleton == null) {
                    singleton = new ConnectManageTemp();
                }
            }
        }
        return singleton;
    }

    public void setProtocal(RpcProtocal protocal) {
        this.protocal = protocal;
    }

    public void start() throws Exception {
        bootstrap = new Bootstrap();
        loopGroup = new NioEventLoopGroup();
        bootstrap.group(loopGroup).channel(NioSocketChannel.class)
                 .handler(new RpcConsumerInitializer(protocal))
                 .option(ChannelOption.SO_KEEPALIVE,true);
    }

    private void connectProvider(InetSocketAddress serverAddress) throws Exception{
        ChannelFuture channelFuture = bootstrap.connect(serverAddress).sync();
        channelFuture.addListener( (ChannelFutureListener) channelFuture1 ->{
            logger.info("connected to the server {}",serverAddress);
            channelMap.put(serverAddress,channelFuture1.channel());
        });
    }

    public void updateConnectedServer(List<InetSocketAddress> allServerAddress) {
        if (allServerAddress != null) {
            if (allServerAddress.size() > 0) {  // Get available server node

                // Add new server node
                for (final InetSocketAddress serverNodeAddress : allServerAddress) {
                    if (!channelMap.keySet().contains(serverNodeAddress)) {
                        try {
                            connectProvider(serverNodeAddress);
                        } catch (Exception e) {
                            logger.error("Connect exception!",e);
                        }
                    }
                }

                // Close and remove invalid server nodes
                for (int i = 0; i < channelMap.size(); ++i) {
                    Channel channel = channelMap.get(i);
                    SocketAddress remotePeer = channel.remoteAddress();
                    if (!allServerAddress.contains(remotePeer)) {
                        logger.info("Remove invalid server node " + remotePeer);
                        if (channel != null) {
                            channel.close();
                        }
                        channelMap.remove(remotePeer);
                    }
                }

            } else { // No available server node ( All server nodes are down )
                logger.error("No available server node. All server nodes are down !!!");
                for (Channel channel : channelMap.values()) {
                    InetSocketAddress remotePeer = (InetSocketAddress) channel.remoteAddress();
                    channel.close();
                    channelMap.remove(remotePeer);
                }
            }
        }
    }

    private Channel chooseChannel() {
        int size = channelMap.size();
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return channelMap.get(index);
    }

    public RPCFuture sendRequest(RpcRequest request,Boolean isAsync) {
        Channel channel = chooseChannel();
        final CountDownLatch latch = new CountDownLatch(1);
        RPCFuture rpcFuture = new RPCFuture(request,isAsync);
        pendingRpcs.put(request.getRequestId(), rpcFuture);
        channel.writeAndFlush(request).addListener((ChannelFutureListener) future -> latch.countDown());
        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error(e.getMessage());
        }

        return rpcFuture;
    }





}
