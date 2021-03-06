package top.yunzhitan.transport;

/**
 * 传输层协议头
 *
 * **************************************************************************************************
 *                                          Protocol
 *  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐
 *       2   │   1   │    1   │     8     │      4      │
 *  ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┤
 *           │       │        │           │             │
 *  │  MAGIC   Sign    Status   Invoke Id   Body Length                   Body Content              │
 *           │       │        │           │             │
 *  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘
 *
 * 消息头16个字节定长
 * = 2 // magic = (short) 0x1a4f
 * + 1 // 消息标志位, 低地址4位用来表示消息类型request/response/heartbeat等, 高地址4位用来表示序列化类型
 * + 1 // 状态位, 设置请求响应状态
 * + 8 // 消息 id, long 类型, 未来jupiter可能将id限制在48位, 留出高地址的16位作为扩展字段
 * + 4 // 消息体 body 长度, int 类型

**/
public class ProtocolHeader {

        /** 协议头长度 */
        public static final int HEAD_LENGTH = 16;
        /** Magic */
        public static final short MAGIC = (short) 0x1a4f;

        /** Message Code: 0x01 ~ 0x0f =================================================================================== */
        public static final byte REQUEST                    = 0x01;     // Request
        public static final byte RESPONSE                   = 0x02;     // Response
        public static final byte PUBLISH_SERVICE            = 0x03;     // 发布服务
        public static final byte PUBLISH_CANCEL_SERVICE     = 0x04;     // 取消发布服务
        public static final byte SUBSCRIBE_SERVICE          = 0x05;     // 订阅服务
        public static final byte OFFLINE_NOTICE             = 0x06;     // 通知下线
        public static final byte ACK                        = 0x07;     // Acknowledge
        public static final byte HEARTBEAT                  = 0x08;     // Heartbeat

        private byte messageCode;       // sign 低地址4位
        // 位数限制最多支持15种不同的序列化/反序列化方式
        // protostuff   = 0x01
        // hessian      = 0x02
        // kryo         = 0x03
        // java         = 0x04
        private byte serializerCode;    // sign 高地址4位
        private byte status;            // 响应状态码
        private long id;                // request.invokeId, 用于映射 <id, request, response> 三元组
        private int bodyLength;         // 消息体长度

        /**
         * 将serializerCode与messageCode组合为Sign
         * @param serializerCode
         * @param messageCode
         * @return
         */
        public static byte toSign(byte serializerCode, byte messageCode) {
                return (byte) ((serializerCode << 4) + messageCode);
        }

        /**
         * 通过Sign得到serializerCode和messageCode
         * @param sign
         */
        public void sign(byte sign) {
                this.messageCode = (byte) (sign & 0x0f);
                this.serializerCode = (byte) ((((int) sign) & 0xff) >> 4);
        }

        public byte messageCode() {
                return messageCode;
        }

        public byte serializerCode() {
                return serializerCode;
        }

        public byte status() {
                return status;
        }

        public void status(byte status) {
                this.status = status;
        }

        public long id() {
                return id;
        }

        public void id(long id) {
                this.id = id;
        }

        public int bodyLength() {
                return bodyLength;
        }

        public void bodyLength(int bodyLength) {
                this.bodyLength = bodyLength;
        }

        @Override
        public String toString() {
                return "ProtocolHeader{" +
                        "messageCode=" + messageCode +
                        ", getSerializerCode=" + serializerCode +
                        ", status=" + status +
                        ", id=" + id +
                        ", bodyLength=" + bodyLength +
                        '}';
        }
}

