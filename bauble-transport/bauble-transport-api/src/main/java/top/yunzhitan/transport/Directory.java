package top.yunzhitan.transport;

public abstract class Directory {

    private String directoryCache;

    /** 服务所属组别 */
    public abstract String getGroup();

    /** 服务名称 */
    public abstract String getServiceName();

    /** 服务版本号 */
    public abstract String getVersion();

    public String directory() {
        if (directoryCache != null) {
            return directoryCache;
        }

        StringBuilder buf = new StringBuilder();
        buf.append(getGroup())
                .append('-')
                .append(getServiceName())
                .append('-')
                .append(getVersion());

        directoryCache = buf.toString();

        return directoryCache;
    }

    public void clear() {
        directoryCache = null;
    }
}

