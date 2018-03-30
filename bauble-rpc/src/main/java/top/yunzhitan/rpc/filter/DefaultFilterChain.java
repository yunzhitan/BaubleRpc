package top.yunzhitan.rpc.filter;

import top.yunzhitan.rpc.model.RpcRequest;

public class DefaultFilterChain implements FilterChain {

    private final Filter filter;
    private final FilterChain next;

    public DefaultFilterChain(Filter filter, FilterChain next) {
        this.filter = filter;
        this.next = next;
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

    @Override
    public FilterChain getNext() {
        return next;
    }

    @Override
    public <T extends FilterContext> void doFilter(RpcRequest request, T filterCtx) throws Throwable {
        filter.doFilter(request,filterCtx);
    }
}
