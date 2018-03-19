package top.yunzhitan.rpc.filter;

import top.yunzhitan.rpc.model.RpcRequest;

public interface FilterChain {

    Filter getFilter();

    FilterChain getNext();

    <T extends FilterContext> void doFilter(RpcRequest request,T filter) throws Throwable;
}
