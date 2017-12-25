package com.yunzhitan.protocol;

import java.io.IOException;

public interface RpcSerialize {

    <T> byte[] serialize(T msg) throws IOException;
    <T> T deserialize(byte[] input, Class<T> cls) throws IOException;

}
