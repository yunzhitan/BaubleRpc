/*
 * Copyright (c) 2015 The Jupiter Project
 *
 * Licensed under the Apache License, version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package top.yunzhitan.serialization.kryo;


import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.esotericsoftware.kryo.serializers.JavaSerializer;
import org.objenesis.strategy.StdInstantiatorStrategy;
import top.yunzhitan.Util.collection.ConcurrentSet;
import top.yunzhitan.serialization.Serializer;
import top.yunzhitan.serialization.SerializerType;

public class KryoSerializer extends Serializer {

    private static ConcurrentSet<Class<?>> useJavaSerializerTypes = new ConcurrentSet<>();

    static {
        useJavaSerializerTypes.add(Throwable.class);
    }

    @SuppressWarnings("unchecked")
    private static final ThreadLocal<Kryo> kryoThreadLocal = new ThreadLocal<Kryo>() {

        @Override
        protected Kryo initialValue() {
            Kryo kryo = new Kryo();
            for (Class<?> type : useJavaSerializerTypes) {
                kryo.addDefaultSerializer(type, JavaSerializer.class);
            }
            kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
            kryo.setRegistrationRequired(false);
            kryo.setReferences(false);
            return kryo;
        }
    };

    private static final ThreadLocal<Output> outputThreadLocal = new ThreadLocal<Output>() {

        @Override
        protected Output initialValue() {
            return new Output(DEFAULT_BUF_SIZE, -1);
        }
    };

    /**
     * Serializes {@code type}'s objects using Java's built in serialization mechanism,
     * note that this is very inefficient and should be avoided if possible.
     */
    public static void setJavaSerializer(Class<?> type) {
        useJavaSerializerTypes.add(type);
    }

    @Override
    public byte getCode() {
        return SerializerType.KRYO.value();
    }

    @Override
    public <T> byte[] writeObject(T obj) {
        Output output = outputThreadLocal.get();
        try {
            Kryo kryo = kryoThreadLocal.get();
            kryo.writeObject(output, obj);
            return output.toBytes();
        } finally {
            output.clear();

            if (output.getBuffer().length > MAX_CACHED_BUF_SIZE) {
                output.setBuffer(new byte[DEFAULT_BUF_SIZE], -1);
            }
        }
    }

    @Override
    public <T> T readObject(byte[] bytes, int offset, int length, Class<T> clazz) {
        Input input = new Input(bytes, offset, length);
        Kryo kryo = kryoThreadLocal.get();
        return kryo.readObject(input, clazz);
    }

    @Override
    public String toString() {
        return "kryo:(getCode=" + getCode() + ")";
    }
}
