package top.yunzhitan.transport;

public class BytesHolder {

    protected byte serializerCode;
    protected byte[] bytes;

    public void bytes(byte serializerCode,byte[] bytes) {
        this.serializerCode = serializerCode;
        this.bytes = bytes;
    }

    public void setSerializerCode(byte serializerCode) {
        this.serializerCode = serializerCode;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getbytes() {
        return bytes;
    }

    public void nullBytes() {
        bytes = null; // help gc
    }

    public int size() {
        return bytes == null ? 0 : bytes.length;
    }
}

