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

package top.yunzhitan.rpc.exception;

import java.net.SocketAddress;


public class SerializationException extends RemoteException {

    private static final long serialVersionUID = -5079093080483380586L;

    public SerializationException() {}

    public SerializationException(SocketAddress remoteAddress) {
        super(remoteAddress);
    }

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, SocketAddress remoteAddress) {
        super(message, remoteAddress);
    }

    public SerializationException(Throwable cause) {
        super(cause);
    }

    public SerializationException(Throwable cause, SocketAddress remoteAddress) {
        super(cause, remoteAddress);
    }
}
