package top.yunzhitan.rpc.filter;

import top.yunzhitan.rpc.model.RpcRequest;

public interface Filter {

    Type getType();

    <T extends FilterContext> void doFilter(RpcRequest request, T filterCtx,FilterChain next) throws Throwable;


    enum Type {
        CONSUMER,
        PROVIDER,
        ALL
    }
}
