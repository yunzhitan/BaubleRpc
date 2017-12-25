package com.yunzhitan.Util;

import java.net.InetSocketAddress;

public class AddressUtil {

    public static InetSocketAddress getSocketAddress(String socketAddress) {
        String[] address = socketAddress.split(":");
        String hostname = address[0];
        int port = Integer.parseInt(address[1]);
        return new InetSocketAddress(hostname, port);
    }
}
