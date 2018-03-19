package top.yunzhitan.rpc.invoker;

public enum InvokerType {
    SYNC,   //同步调用
    ASYNC;  //异步调用

    public static InvokerType parse(String name) {
        for(InvokerType type : values()) {
            if(type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public static InvokerType getDefault() {
        return SYNC;
    }
}
