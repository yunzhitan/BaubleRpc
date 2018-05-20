package top.yunzhitan.Util;

import sun.misc.Unsafe;
import top.yunzhitan.common.UnsafeReferenceFieldUpdater;
import top.yunzhitan.common.UnsafeUpdater;

public class ThrowUtil {

    private static final UnsafeReferenceFieldUpdater<Throwable, Throwable> causeUpdater =
            UnsafeUpdater.newReferenceFieldUpdater(Throwable.class, "cause");

    /**
     * Raises an exception bypassing compiler checks for checked exceptions.
     */
    public static void throwException(Throwable t) {
        Unsafe unsafe = UnsafeUtil.getUnsafe();
        if (unsafe != null) {
            unsafe.throwException(t);
        } else {
            ThrowUtil.throwException0(t);
        }
    }

    @SuppressWarnings("unchecked")
    private static <E extends Throwable> void throwException0(Throwable t) throws E {
        throw (E) t;
    }

    public static <T extends Throwable> T cutCause(T cause) {
        Throwable rootCause = cause;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        if (rootCause != cause) {
            cause.setStackTrace(rootCause.getStackTrace());
            assert causeUpdater != null;
            causeUpdater.set(cause, cause);
        }
        return cause;
    }

    private ThrowUtil() {}
}

