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

package top.yunzhitan.serialization;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.Util.BaubleServiceLoader;

import java.util.HashMap;
import java.util.Map;

public final class SerializerFactory {

    private static final Logger logger = LoggerFactory.getLogger(SerializerFactory.class);

    private static final Map<Byte,Serializer> serializers = new HashMap<>();

    public SerializerFactory() {
    }

    static {
        Iterable<Serializer> all = BaubleServiceLoader.load(Serializer.class);
        for (Serializer s : all) {
            serializers.put(s.getCode(), s);
        }
    }

    public static Serializer getSerializer(byte code) {
        Serializer serializer = serializers.get(code);

        if (serializer == null) {
            SerializerType type = SerializerType.parse(code);
            if (type != null) {
                throw new IllegalArgumentException("serializer implementation [" + type.name() + "] not found");
            } else {
                throw new IllegalArgumentException("unsupported serializer type with getCode: " + code);
            }
        }

        return serializer;
    }
}
