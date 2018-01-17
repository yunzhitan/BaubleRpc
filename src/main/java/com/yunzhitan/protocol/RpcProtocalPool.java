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
        protocalMap.put(RpcProtocal.PROTOSTUFF,new ProtostuffSerialize());
        protocalMap.put(RpcProtocal.HESSIAN,new HessianSerialize());
        protocalMap.put(RpcProtocal.JSON,new JsonSerialize());
    }

    public static RpcProtocalPool getInstance() {
        return singleton;
    }

    public RpcSerialize getProtocal(RpcProtocal protocal) {
            return protocalMap.get(protocal);
    }
}
