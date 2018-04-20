package top.yunzhitan.Util.id;

public interface IdServiceFactory {

    /**
     * 根据业务标签获取相应的ID
     * @param biztag
     * @return
     */

    Long getIdByBizTag(String biztag);
}
