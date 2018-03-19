package top.yunzhitan.transport;

import top.yunzhitan.Util.SocketAddress;

public abstract class Connection {
    private final SocketAddress address;

    public Connection(SocketAddress address) {
        this.address = address;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public void operationComplete(@SuppressWarnings("unused") Runnable callback) {
        // the default implementation does nothing
    }

    public abstract void setReconnect(boolean reconnect);

}
