package top.yunzhitan.serialization.io;

import java.io.ByteArrayOutputStream;

import static top.yunzhitan.serialization.Serializer.DEFAULT_BUF_SIZE;

public class OutputStreams {
    private static final ReferenceFieldUpdater<ByteArrayOutputStream, byte[]> bufUpdater =
            UnsafeUpdater.newReferenceFieldUpdater(ByteArrayOutputStream.class, "buf");

    // 复用 ByteArrayOutputStream 中的 byte[]
    @SuppressWarnings("unchecked")
    private static final ThreadLocal<ByteArrayOutputStream> bufThreadLocal = new ThreadLocal() {

        @Override
        protected ByteArrayOutputStream initialValue() {
            return new ByteArrayOutputStream(DEFAULT_BUF_SIZE);
        }
    };

}
