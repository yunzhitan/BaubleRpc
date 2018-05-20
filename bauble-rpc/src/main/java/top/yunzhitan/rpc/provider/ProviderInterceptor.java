package top.yunzhitan.rpc.provider;

public interface ProviderInterceptor {

    /**
     * this method is executed before the method is optionally called
     * @param provider  provider to be intercepted
     * @param methodName name of the called method
     * @param args       arguments of the called method
     */
    void beforeInvoke(Object provider, String methodName, Object[] args);

    /**
     * This getCode is executed after the method is optionally called.
     *
     * @param provider      provider be intercepted
     * @param methodName    name of the called method
     * @param args          arguments to the called method
     * @param result        result of the call, in the case of a call failure, {@code result}  is null
     */
    void afterInvoke(Object provider, String methodName, Object[] args, Object result);

}
