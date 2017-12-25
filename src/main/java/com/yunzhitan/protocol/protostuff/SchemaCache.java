package com.yunzhitan.protocol.protostuff;

import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class SchemaCache {
    private final Cache schemaCache = CacheBuilder.newBuilder()
            .maximumSize(1024).expireAfterWrite(1, TimeUnit.HOURS)
            .build();
    private volatile static SchemaCache singleton;

    public static SchemaCache getInstance(){
        if(singleton == null) {
            synchronized (SchemaCache.class) {
                if (singleton == null) {
                    singleton = new SchemaCache();
                }
            }
        }
        return singleton;
    }

    /**
     * 返回给定的Schema，或者将给定的Callable运算结果加入缓存中。
     * @param cls
     * @return
     */
    public <T> Schema<T> getSchema(Class<T>cls) {
        try {
            return (Schema<T>) schemaCache.get(cls, (Callable<RuntimeSchema<T>>) () -> RuntimeSchema.createFrom(cls));
        } catch (ExecutionException e) {
            return null;
        }
    }

}
