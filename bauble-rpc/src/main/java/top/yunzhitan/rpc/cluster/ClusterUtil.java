package top.yunzhitan.rpc.cluster;

import top.yunzhitan.rpc.consumer.Dispatcher;
import top.yunzhitan.rpc.model.ClusterTypeConfig;
import top.yunzhitan.rpc.model.MethodSpecialConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterUtil {

    private final ClusterInvoker defaultClusterInvoker;
    private final Map<String,ClusterInvoker> methodSpecialClusterInvoker;

    public ClusterUtil(Dispatcher dispatcher,
                                   ClusterTypeConfig defaultStrategy,
                                   List<MethodSpecialConfig> methodSpecialConfigs) {
        this.defaultClusterInvoker = createClusterInvoker(dispatcher, defaultStrategy);
        this.methodSpecialClusterInvoker = new HashMap<>();

        for (MethodSpecialConfig config : methodSpecialConfigs) {
            ClusterTypeConfig strategy = config.getStrategy();
            if (strategy != null) {
                methodSpecialClusterInvoker.put(
                        config.getMethodName(),
                        createClusterInvoker(dispatcher, strategy)
                );
            }
        }
    }

    public ClusterInvoker findClusterInvoker(String methodName) {
        ClusterInvoker invoker = methodSpecialClusterInvoker.get(methodName);
        return invoker != null ? invoker : defaultClusterInvoker;
    }

    private ClusterInvoker createClusterInvoker(Dispatcher dispatcher, ClusterTypeConfig strategy) {
        ClusterType s = strategy.getClusterType();
        switch (s) {
            case FAIL_FAST:
                return new FailFastClusterInvoker(dispatcher);
            case FAIL_OVER:
                return new FailOverClusterInvoker(dispatcher, strategy.getFailoverRetries());
            case FAIL_SAFE:
                return new FailSafeClusterInvoker(dispatcher);
            default:
                throw new UnsupportedOperationException("strategy: " + strategy);
        }
    }

}
