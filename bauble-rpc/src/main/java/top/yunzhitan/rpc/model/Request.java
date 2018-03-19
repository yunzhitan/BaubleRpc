package top.yunzhitan.rpc.model;

public interface Request {

    /**
     * get method name.
     *
     * @return method name.
     * @serial
     */
    String getMethodName();

    /**
     * get parameter types.
     *
     * @return parameter types.
     * @serial
     */

    Object[] getArguments();

    /**
     * get attachments.
     *
     * @return attachments.
     * @serial
     */

}
