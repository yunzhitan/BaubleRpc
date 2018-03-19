package top.yunzhitan.rpc.model;

public interface Response {

    /**
     * Get invoke result.
     *
     * @return result. if no result return null.
     */
    Object getResult();

    /**
     * Get exception.
     *
     * @return exception. if no exception return null.
     */
    Throwable getException();

    /**
     * Has exception.
     *
     * @return has exception.
     */
    boolean hasException();

}
