package com.yunzhitan.protocol;

import com.yunzhitan.protocol.hessian.HessianSerialize;
import com.yunzhitan.protocol.json.JsonSerialize;
import com.yunzhitan.protocol.protostuff.ProtostuffSerialize;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RpcProtocalPool {

    private Map<RpcProtocal,RpcSerialize> protocalMap = new ConcurrentHashMap<>();
    private volatile static RpcProtocalPool singleton = new RpcProtocalPool();

    private RpcProtocalPool(){
        protocalMap.put(RpcProtocal.PROTOSTUFFSERIALIZE,new ProtostuffSerialize());
        protocalMap.put(RpcProtocal.HESSIANSERIALIZE,new HessianSerialize());
        protocalMap.put(RpcProtocal.JSONSERIALIZE,new JsonSerialize());
    }

    public static RpcProtocalPool getInstance() {
        return singleton;
    }

    public RpcSerialize getProtocal(RpcProtocal protocal) {
            return protocalMap.get(protocal);
    }
}
