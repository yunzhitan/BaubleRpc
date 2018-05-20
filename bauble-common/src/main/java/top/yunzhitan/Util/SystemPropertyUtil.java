package top.yunzhitan.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.regex.Pattern;

/**
 * 系统信息类
 */

public class SystemPropertyUtil {

    private static final Logger logger = LoggerFactory.getLogger(SystemPropertyUtil.class);

    public static final int AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors();

    /**
     * 机器的工作ID，用于分布式ID生成
     */
    public static final int WORKER_ID = 12;

    /**
     * Returns {@code true} if and only if the system property with the specified {@code key}
     * exists.
     */
    public static boolean contains(String key) {
            return get(key) != null;
        }
        /**
         * * Returns the value of the Java system property with the specified
         * {@code key}, while falling back to {@code null} if the property access fails.
         *
         * @return the property value or {@code null}
         */
        public static String get(String key) {
            return get(key, null);
        }

        /**
         * Returns the value of the Java system property with the specified
         * {@code key}, while falling back to the specified default value if
         * the property access fails.
         *
         * @return the property value.
         *         {@code def} if there's no such property or if an access to the
         *         specified property is not allowed.
         */
        public static String get(final String key, String def) {
            if (key == null) {
                throw new NullPointerException("key");
            }
            if (key.isEmpty()) {
                throw new IllegalArgumentException("key must not be empty.");
            }

            String value = null;
            try {
                if (System.getSecurityManager() == null) {
                    value = System.getProperty(key);
                } else {
                    value = AccessController.doPrivileged((PrivilegedAction<String>) () -> System.getProperty(key));
                }
            } catch (Exception e) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Unable to retrieve a system property '{}'; default values will be used, {}.", key, e);
                }
            }

            if (value == null) {
                return def;
            }

            return value;
        }

        /**
         * Returns the value of the Java system property with the specified
         * {@code key}, while falling back to the specified default value if
         * the property access fails.
         *
         * @return the property value.
         *         {@code def} if there's no such property or if an access to the
         *         specified property is not allowed.
         */
        public static boolean getBoolean(String key, boolean def) {
            String value = get(key);
            if (value == null) {
                return def;
            }

            value = value.trim().toLowerCase();
            if (value.isEmpty()) {
                return true;
            }

            if ("true".equals(value) || "yes".equals(value) || "1".equals(value)) {
                return true;
            }

            if ("false".equals(value) || "no".equals(value) || "0".equals(value)) {
                return false;
            }

            logger.warn("Unable to parse the boolean system property '{}':{} - using the default value: {}.", key, value, def);

            return def;
        }

        private static final Pattern INTEGER_PATTERN = Pattern.compile("-?[0-9]+");

        /**
         * Returns the value of the Java system property with the specified
         * {@code key}, while falling back to the specified default value if
         * the property access fails.
         *
         * @return the property value.
         *         {@code def} if there's no such property or if an access to the
         *         specified property is not allowed.
         */
        public static int getInt(String key, int def) {
            String value = get(key);
            if (value == null) {
                return def;
            }

            value = value.trim().toLowerCase();
            if (INTEGER_PATTERN.matcher(value).matches()) {
                try {
                    return Integer.parseInt(value);
                } catch (Exception ignored) {}
            }

            logger.warn("Unable to parse the integer system property '{}':{} - using the default value: {}.", key, value, def);

            return def;
        }

        /**
         * Returns the value of the Java system property with the specified
         * {@code key}, while falling back to the specified default value if
         * the property access fails.
         *
         * @return the property value.
         *         {@code def} if there's no such property or if an access to the
         *         specified property is not allowed.
         */
        public static long getLong(String key, long def) {
            String value = get(key);
            if (value == null) {
                return def;
            }

            value = value.trim().toLowerCase();
            if (INTEGER_PATTERN.matcher(value).matches()) {
                try {
                    return Long.parseLong(value);
                } catch (Exception ignored) {}
            }

            logger.warn("Unable to parse the long integer system property '{}':{} - using the default value: {}.", key, value, def);

            return def;
        }

        /**
         * Sets the value of the Java system property with the specified {@code key}
         */
        public static Object setProperty(String key, String value) {
            return System.getProperties().setProperty(key, value);
        }

        private SystemPropertyUtil() {}
    }
