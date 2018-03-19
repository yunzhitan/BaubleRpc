package top.yunzhitan.rpc.id;

public interface IdServiceFactory {

    /**
     * 根据业务标签获取相应的ID
     * @param biztag
     * @return
     */

    public Long getIdByBizTag(String biztag);
}
