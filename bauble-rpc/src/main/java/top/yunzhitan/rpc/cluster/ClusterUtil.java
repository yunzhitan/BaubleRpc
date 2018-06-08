package top.yunzhitan.rpc.cluster;

import top.yunzhitan.rpc.consumer.transporter.Transporter;
import top.yunzhitan.rpc.model.ClusterTypeConfig;
import top.yunzhitan.rpc.model.MethodSpecialConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClusterUtil {

    private final ClusterInvoker defaultClusterInvoker;
    private final Map<String,ClusterInvoker> methodSpecialClusterInvoker;

    public ClusterUtil(Transporter transporter,
                                   ClusterTypeConfig defaultStrategy,
                                   List<MethodSpecialConfig> methodSpecialConfigs) {
        this.defaultClusterInvoker = createClusterInvoker(transporter, defaultStrategy);
        this.methodSpecialClusterInvoker = new HashMap<>();

        for (MethodSpecialConfig config : methodSpecialConfigs) {
            ClusterTypeConfig strategy = config.getTypeConfig();
            if (strategy != null) {
                methodSpecialClusterInvoker.put(
                        config.getMethodName(),
                        createClusterInvoker(transporter, strategy)
                );
            }
        }
    }

    public ClusterInvoker findClusterInvoker(String methodName) {
        ClusterInvoker invoker = methodSpecialClusterInvoker.get(methodName);
        return invoker != null ? invoker : defaultClusterInvoker;
    }

    private ClusterInvoker createClusterInvoker(Transporter transporter, ClusterTypeConfig strategy) {
        ClusterType s = strategy.getClusterType();
        switch (s) {
            case FAIL_FAST:
                return new FailFastClusterInvoker(transporter);
            case FAIL_OVER:
                return new FailOverClusterInvoker(transporter, strategy.getFailoverRetries());
           default:
                throw new UnsupportedOperationException("strategy: " + strategy);
        }
    }

}
