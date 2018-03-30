package top.yunzhitan.transport;

import io.netty.channel.group.DefaultChannelGroup;
import top.yunzhitan.transport.processor.ClientProcessor;

import java.net.SocketAddress;
import java.util.Collection;

public interface Client<C> {
    /**
     * Returns the rpc processor.
     */
    ClientProcessor getProcessor();

    /**
     * Binds the rpc processor.
     */
    void setProcessor(ClientProcessor processor);

    /**
     * Connects to the remote peer.
     */
    C connect(SocketAddress address);

    /**
     * Connects to the remote peer.
     */
    C connect(SocketAddress address, boolean async);

    /**
     * Returns or new a {@link DefaultChannelGroup}.
     */
    DefaultChannelGroup getGroup(SocketAddress address);

    /**
     * Returns all {@link DefaultChannelGroup}s.
     */
    Collection<DefaultChannelGroup> getGroups();

    /**
     * Adds a {@link DefaultChannelGroup} by {@link Directory}.
     */
    boolean addChannelGroup(Directory directory, DefaultChannelGroup group);

    /**
     * Removes a {@link DefaultChannelGroup} by {@link Directory}.
     */
    boolean removeChannelGroup(Directory directory, DefaultChannelGroup group);

    /**
     * Returns list of {@link DefaultChannelGroup}s by the same {@link Directory}.
     */

    /**
     * Shutdown the server.
     */
    void shutdownGracefully();

    interface ConnectionWatcher {

        /**
         * Start to connect to server.
         */
        void start();

        /**
         * Wait until the connections is available or timeout,
         * if available return true, otherwise return false.
         */
        boolean waitForAvailable(long timeoutMillis);
    }

}
