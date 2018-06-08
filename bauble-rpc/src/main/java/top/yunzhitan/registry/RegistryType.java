package top.yunzhitan.registry;

public enum RegistryType {
    DEFAULT("default"),
    ZOOKEEPER("zookeeper");

    private final String value;

    RegistryType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static RegistryType parse(String data) {
        for(RegistryType type : values()) {
            if(type.name().equalsIgnoreCase(data)) {
                return type;
            }
        }
        return null;
    }
}
