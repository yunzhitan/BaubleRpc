package top.yunzhitan.rpc;

public enum DispatchType {
    ROUND,            //单播
    BROADCAST;        //广播

    public static DispatchType parse(String name) {
        for(DispatchType type : values()) {
            if(type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public static DispatchType getDefault() {
        return ROUND;
    }
}
