package top.yunzhitan.rpc.provider;

import com.sun.deploy.trace.Trace;
import top.yunzhitan.rpc.tracing.TraceId;

public interface ProviderInterceptor {

    /**
     * this method is executed before the method is optionally called
     * @param traceId   id for tracing, may be null if without tracing
     * @param provider  provider to be intercepted
     * @param methodName name of the called method
     * @param args       arguments of the called method
     */
    void beforeInvoke(TraceId traceId, Object provider, String methodName, Object[] args);

    /**
     * This code is executed after the method is optionally called.
     *
     * @param traceId       id for tracing, may be null if without tracing
     * @param provider      provider be intercepted
     * @param methodName    name of the called method
     * @param args          arguments to the called method
     * @param result        result of the call, in the case of a call failure, {@code result}  is null
     * @param failCause     exception of the call, in the case of a call succeeds, {@code failCause}  is null
     */
    void afterInvoke(TraceId traceId, Object provider, String methodName, Object[] args, Object result, Throwable failCause);

}
