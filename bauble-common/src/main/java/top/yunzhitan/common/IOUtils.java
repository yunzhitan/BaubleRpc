package top.yunzhitan.common;

import java.io.Closeable;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * IO工具类
 *
 * @author <a href="mailto:zhanggeng.zg@antfin.com">GengZhang</a>
 */
public class IOUtils {

    /**
     * 静默关闭
     *
     * @param closeable 可关闭的
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignore) {
                // NOPMD
            }
        }
    }

    /**
     * 静默关闭 for jdk6
     *
     * @param closeable 可关闭的
     */
    public static void closeQuietly(ServerSocket closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignore) {
                // NOPMD
            }
        }
    }

    /**
     * 静默关闭 for jdk6
     *
     * @param closeable 可关闭的
     */
    public static void closeQuietly(Socket closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception ignore) {
                // NOPMD
            }
        }
    }
}
