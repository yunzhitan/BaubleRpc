package top.yunzhitan.transport;

import java.net.InetSocketAddress;

public abstract class AbstractChannel<CONTEXT, CHANNEL> {

    /**
     * 长连接上下文
     *
     * @return ChannelContext
     */
    public abstract CONTEXT channelContext();

    /**
     * 通道
     *
     * @return Channel
     */
    public abstract CHANNEL channel();

    /**
     * 得到连接的远端地址
     *
     * @return the remote address
     */
    public abstract InetSocketAddress remoteAddress();

    /**
     * 得到连接的本地地址（如果是短连接，可能不准）
     *
     * @return the local address
     */
    public abstract InetSocketAddress localAddress();

    /**
     * 写入数据
     *
     * @param obj data which need to write
     */
    public abstract void writeAndFlush(Object obj);

    /**
     * 是否可用
     *
     * @return 是否可以
     */
    public abstract boolean isAvailable();
}

