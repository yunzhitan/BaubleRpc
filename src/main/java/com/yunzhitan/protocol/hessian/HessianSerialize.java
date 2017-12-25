package com.yunzhitan.protocol.hessian;

import com.caucho.hessian.io.Hessian2Input;
import com.caucho.hessian.io.Hessian2Output;
import com.yunzhitan.protocol.RpcSerialize;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class HessianSerialize implements RpcSerialize {

    @Override
    public <T> byte[] serialize(T msg) throws IOException {
        ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
        Hessian2Output h20 = new Hessian2Output(byteOutputStream);
        h20.startMessage();
        h20.writeObject(msg);
        h20.completeMessage();
        byte[] body = byteOutputStream.toByteArray();
        h20.close();
        byteOutputStream.close();
        return body;
    }

    @Override
    public <T> T deserialize(byte[] body, Class<T> cls) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(body);
        Hessian2Input h2I = new Hessian2Input(inputStream);
        h2I.startMessage();
        T object = (T) h2I.readObject();
        h2I.completeMessage();
        h2I.close();
        inputStream.close();
        return object;
    }
}
