package com.yunzhitan.protocol.protostuff;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.yunzhitan.protocol.RpcSerialize;

import java.io.IOException;

public class ProtostuffSerialize implements RpcSerialize {

    private static <T> Schema<T> getSchema(Class<T> tClass) {
        return SchemaCache.getInstance().getSchema(tClass);
    }

    @Override
    public <T> byte[] serialize(T msg) throws IOException {
        Class<T> tclass = (Class<T>) msg.getClass();
        LinkedBuffer linkedBuffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try{
            Schema<T> schema = getSchema(tclass);
            return ProtostuffIOUtil.toByteArray(msg,schema,linkedBuffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            linkedBuffer.clear();
        }
    }

    @Override
    public <T> T deserialize(byte[] input, Class<T> cls) throws IOException {
        Schema<T> schema = getSchema(cls);
        T message = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(input,message,schema);
        return message;
    }
}
