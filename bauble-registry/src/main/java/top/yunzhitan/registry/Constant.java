package top.yunzhitan.registry;

public interface Constant {

    int ZK_SESSION_TIMEOUT = 500000;

    String ZK_REGISTRY_PATH = "/registry";
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
}
