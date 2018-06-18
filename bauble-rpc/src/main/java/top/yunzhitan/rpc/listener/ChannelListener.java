package top.yunzhitan.rpc.listener;

import top.yunzhitan.transport.AbstractChannel;

public interface ChannelListener {

    /**
     * Handle connect event on channel active
     *
     * @param channel Channel
     */
    public void onConnected(AbstractChannel channel);

    /**
     * Handle disconnect event on channel closed
     *
     * @param channel Channel
     */
    public void onDisconnected(AbstractChannel channel);
}

