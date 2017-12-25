package com.yunzhitan.protocol;

public enum RpcProtocal {
    HESSIANSERIALIZE("hessian"), PROTOSTUFFSERIALIZE("protostuff"),JSONSERIALIZE("json");
    private String serializrProtocal;

    RpcProtocal(String serializrProtocal) {
        this.serializrProtocal = serializrProtocal;
    }

    public String getProtocal() {
        return serializrProtocal;
    }

    @Override
    public String toString() {
        return serializrProtocal;
    }
}
