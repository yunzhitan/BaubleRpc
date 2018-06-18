package top.yunzhitan.rpc.cluster;

import top.yunzhitan.Util.SPI;
import top.yunzhitan.rpc.exception.RemoteException;
import top.yunzhitan.rpc.exception.RpcException;
import top.yunzhitan.rpc.model.ProviderConfig;
import top.yunzhitan.rpc.model.RpcRequest;
import top.yunzhitan.rpc.model.RpcResponse;

@SPI(name = "fail_fast")
public class FailFastCluster extends Cluster{

    @Override
    public RpcResponse doInvoke(RpcRequest request) throws RpcException {
        ProviderConfig providerConfig = select(request);
        try {
            RpcResponse response = filterChain(providerConfig, request);
            if (response != null) {
                return response;
            } else {
                throw new RpcException(
                        "Failed to call " + request.getServiceConfig()
                                + " on remote server " + providerConfig);
            }
        } catch (Exception e) {
            throw new RpcException(
                    "Failed to call " + request.getServiceConfig()
                            + " on remote server: " + providerConfig + ", cause by: "
                            + e.getClass().getName() + ", message is: " + e.getMessage());
        }
    }

    @Override
    public RpcResponse sendMsg(ProviderConfig providerConfig, RpcRequest request) throws RemoteException {
        return null;
    }
}
