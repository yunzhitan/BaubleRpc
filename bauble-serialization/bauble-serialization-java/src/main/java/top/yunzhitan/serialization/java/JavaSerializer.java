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

package top.yunzhitan.serialization.java;


import top.yunzhitan.Util.ThrowUtil;
import top.yunzhitan.serialization.Serializer;
import top.yunzhitan.serialization.SerializerType;

import java.io.*;

/**
 * Java自身的序列化/反序列化实现.
 *
 * jupiter
 * org.jupiter.serialization.java
 *
 * @author jiachun.fjc
 */
public class JavaSerializer extends Serializer {

    @SuppressWarnings("unchecked")
    private static final ThreadLocal<ByteArrayOutputStream> bufThreadLocal = new ThreadLocal() {

        @Override
        protected ByteArrayOutputStream initialValue() {
            return new ByteArrayOutputStream(DEFAULT_BUF_SIZE);
        }
    };

    @Override
    public byte code() {
        return SerializerType.JAVA.value();
    }

    @Override
    public <T> byte[] writeObject(T obj) {
        ByteArrayOutputStream buf = bufThreadLocal.get();
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(buf);
            output.writeObject(obj);
            output.flush();
            return buf.toByteArray();
        } catch (IOException e) {
            ThrowUtil.throwException(e);
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException ignored) {}
            }

            buf.reset(); // for reuse
        }
        return null;
    }

    @Override
    public <T> T readObject(byte[] bytes, int offset, int length, Class<T> clazz) {
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new ByteArrayInputStream(bytes, offset, length));
            Object obj = input.readObject();
            return clazz.cast(obj);
        } catch (Exception e) {
            ThrowUtil.throwException(e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ignored) {}
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "java:(code=" + code() + ")";
    }
}
