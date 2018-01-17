package com.yunzhitan.client;

import com.yunzhitan.protocol.RpcProtocal;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConnectManage {
    private final Logger logger = LoggerFactory.getLogger(ConnectManage.class);
    private volatile static ConnectManage singleton;
    private RpcProtocal rpcProtocal = RpcProtocal.PROTOSTUFF;

    private EventLoopGroup eventLoopGroup = new NioEventLoopGroup(4);
    private Bootstrap bootstrap;
    private ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16, 600L,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(65536));

    private FixedChannelPool fixedChannelPool;

    private CopyOnWriteArrayList<ConsumerHandler> connectedHandlers = new CopyOnWriteArrayList<>();
    private Map<InetSocketAddress, ConsumerHandler> connectedServerNodes = new ConcurrentHashMap<>();
    //private Map<InetSocketAddress, Channel> connectedServerNodes = new ConcurrentHashMap<>();

    private Lock lock = new ReentrantLock();
    private Condition connected = lock.newCondition();
    private AtomicInteger roundRobin = new AtomicInteger(0);
    private volatile boolean isRunning = true;


    private ConnectManage() {
    }

    public static ConnectManage getInstance() {
        if (singleton == null) {
            synchronized (ConnectManage.class) {
                if (singleton == null) {
                    singleton = new ConnectManage();
                }
            }
        }
        return singleton;
    }

    public void setProtocal(RpcProtocal rpcProtocal) {
        this.rpcProtocal = rpcProtocal;
    }

    public void updateConnectedServer(List<InetSocketAddress> allServerAddress) {
        if (allServerAddress != null) {
            if (allServerAddress.size() > 0) {  // Get available server node

                // Add new server node
                for (final InetSocketAddress serverNodeAddress : allServerAddress) {
                    if (!connectedServerNodes.keySet().contains(serverNodeAddress)) {
                        connectServerNode(serverNodeAddress);
                    }
                }

                // Close and remove invalid server nodes
                for (int i = 0; i < connectedHandlers.size(); ++i) {
                    ConsumerHandler connectedServerHandler = connectedHandlers.get(i);
                    SocketAddress remotePeer = connectedServerHandler.getRemoteAddress();
                    if (!allServerAddress.contains(remotePeer)) {
                        logger.info("Remove invalid server node " + remotePeer);
                        ConsumerHandler handler = connectedServerNodes.get(remotePeer);
                        if (handler != null) {
                            handler.close();
                        }
                        connectedServerNodes.remove(remotePeer);
                        connectedHandlers.remove(connectedServerHandler);
                    }
                }

            } else { // No available server node ( All server nodes are down )
                logger.error("No available server node. All server nodes are down !!!");
                for (final ConsumerHandler connectedServerHandler : connectedHandlers) {
                    SocketAddress remotePeer = connectedServerHandler.getRemoteAddress();
                    ConsumerHandler handler = connectedServerNodes.get(remotePeer);
                    handler.close();
                    connectedServerNodes.remove(connectedServerHandler);
                }
                connectedHandlers.clear();
            }
        }
    }

    public void reconnect(final ConsumerHandler handler, final SocketAddress remotePeer) {
        if (handler != null) {
            connectedHandlers.remove(handler);
            connectedServerNodes.remove(handler.getRemoteAddress());
        }
        connectServerNode((InetSocketAddress) remotePeer);
    }

    private void connectServerNode(final InetSocketAddress remotePeer) {
        threadPoolExecutor.submit(() -> {
            Bootstrap bootstrap = new Bootstrap();  //eventLoopGroup netty框架下的线程池，默认为cpu的二倍
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new RpcConsumerInitializer(rpcProtocal));

            ChannelFuture channelFuture = bootstrap.connect(remotePeer);
            channelFuture.addListener((ChannelFutureListener) channelFuture1 -> {
                if (channelFuture1.isSuccess()) {
                    logger.debug("Successfully connect to remote server. remote peer = " + remotePeer);
                    ConsumerHandler handler = channelFuture1.channel().pipeline().get(ConsumerHandler.class);
                    addHandler(handler);
                }
            });
        });
    }

    private void addHandler(ConsumerHandler handler) {
        connectedHandlers.add(handler);
        InetSocketAddress remoteAddress = (InetSocketAddress) handler.getRemoteAddress();
        connectedServerNodes.put(remoteAddress, handler);
        signalAvailableHandler();
    }

    private void signalAvailableHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private boolean waitingForHandler() throws InterruptedException {
        lock.lock();
        try {
            long connectTimeoutMillis = 1000;
            return connected.await(connectTimeoutMillis, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public ConsumerHandler chooseHandler() {
        CopyOnWriteArrayList<ConsumerHandler> handlers = (CopyOnWriteArrayList<ConsumerHandler>) this.connectedHandlers.clone();
        int size = handlers.size();
        while (isRunning && size <= 0) {
            try {
                boolean available = waitingForHandler();
                if (available) {
                    handlers = (CopyOnWriteArrayList<ConsumerHandler>) this.connectedHandlers.clone();
                    size = handlers.size();
                }
            } catch (InterruptedException e) {
                logger.error("Waiting for available node is interrupted! ", e);
                throw new RuntimeException("Can't connect any servers!", e);
            }
        }
        int index = (roundRobin.getAndAdd(1) + size) % size;
        return handlers.get(index);
    }

    public void stop() {
        isRunning = false;
        for (ConsumerHandler connectedServerHandler : connectedHandlers) {
            connectedServerHandler.close();
        }
        signalAvailableHandler();
        threadPoolExecutor.shutdown();
        eventLoopGroup.shutdownGracefully();
    }
}
