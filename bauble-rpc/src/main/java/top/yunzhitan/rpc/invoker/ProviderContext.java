package top.yunzhitan.rpc.invoker;

import top.yunzhitan.rpc.filter.Filter;
import top.yunzhitan.rpc.filter.FilterContext;
import top.yunzhitan.rpc.model.ServiceProvider;

import static com.google.common.base.Preconditions.checkNotNull;

public class ProviderContext implements FilterContext {

    private final ServiceProvider provider;

    private Object result;                  // 服务调用结果,包括异常
    private Class<?>[] expectCauseTypes;    // 预期内的异常类型

    public ProviderContext(ServiceProvider provider) {
        this.provider = checkNotNull(provider, "provider");
    }

    public ServiceProvider getProvider() {
        return provider;
    }

    public Object getResult() {
        return result;
    }

    @Override
    public Filter.Type getType() {
        return Filter.Type.PROVIDER;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Class<?>[] getExpectCauseTypes() {
        return expectCauseTypes;
    }

    public void setCauseAndExpectTypes(Throwable cause, Class<?>[] expectCauseTypes) {
        this.expectCauseTypes = expectCauseTypes;
    }


}
