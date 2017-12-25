package com.yunzhitan.client.proxy;

import com.yunzhitan.client.RPCFuture;

public interface IAsyncObjectProxy {
    RPCFuture call(String funcName, Object... args);
}