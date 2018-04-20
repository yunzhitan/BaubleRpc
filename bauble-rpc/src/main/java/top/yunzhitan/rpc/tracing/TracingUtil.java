package top.yunzhitan.rpc.tracing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.yunzhitan.Util.NetUtil;
import top.yunzhitan.Util.SystemPropertyUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

public class TracingUtil {

    private static final Logger logger = LoggerFactory.getLogger(TracingUtil.class);

    private static final boolean TRACING_NEEDED = true;

    private static final ThreadLocal<TraceId> traceThreadLocal = new ThreadLocal<>();

    // maximal value for 64bit systems is 2^22, see man 5 proc.
    private static final int MAX_PROCESS_ID = 4194304;
    private static final char PID_FLAG = 'd';
    private static final String IP_16;
    private static final String PID;
    private static final long ID_BASE = 1000;
    private static final long ID_MASK = (1 << 13) - 1; // 8192 - 1
    private static final AtomicLong sequence = new AtomicLong();

    static {
        String _ip_16;
        try {
            String ip = SystemPropertyUtil.get("jupiter.local.address", NetUtil.getLocalAddress());
            _ip_16 = getIP_16(ip);
        } catch (Throwable t) {
            _ip_16 = "ffffffff";
        }
        IP_16 = _ip_16;

        String _pid;
        try {
            _pid = getHexProcessId(getProcessId());
        } catch (Throwable t) {
            _pid = "0000";
        }
        PID = _pid;
    }

    public static boolean isTracingNeeded() {
        return TRACING_NEEDED;
    }

    public static String generateTraceId() {
        return getTraceId(IP_16, System.currentTimeMillis(), getNextId());
    }

    public static TraceId getCurrent() {
        TraceId traceId = null;
        if (TRACING_NEEDED) {
            traceId = traceThreadLocal.get();
        }
        return traceId != null ? traceId : TraceId.NULL_TRACE_ID;
    }

    public static void setCurrent(TraceId traceId) {
        if (traceId == null) {
            traceThreadLocal.remove();
        } else {
            traceThreadLocal.set(traceId);
        }
    }

    public static TraceId safeGetTraceId(TraceId traceId) {
        return traceId == null ? TraceId.NULL_TRACE_ID : traceId;
    }

    public static void clearCurrent() {
        traceThreadLocal.remove();
    }

    private static String getHexProcessId(int pid) {
        // unsigned short 0 to 65535
        if (pid < 0) {
            pid = 0;
        }
        if (pid > 65535) {
            String strPid = Integer.toString(pid);
            strPid = strPid.substring(strPid.length() - 4, strPid.length());
            pid = Integer.parseInt(strPid);
        }
        StringBuilder buf = new StringBuilder(Integer.toHexString(pid));
        while (buf.length() < 4) {
            buf.insert(0, "0");
        }
        return buf.toString();
    }

    /**
     * Gets current pid, max pid 32 bit systems 32768, for 64 bit 4194304
     * http://unix.stackexchange.com/questions/16883/what-is-the-maximum-value-of-the-pid-of-a-process
     * http://stackoverflow.com/questions/35842/how-can-a-java-program-get-its-own-process-id
     */
    private static int getProcessId() {
        String value = "";
        try {
            RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
            value = runtime.getName();
        } catch (Throwable t) {
            if (logger.isDebugEnabled()) {
                logger.debug("Could not filter ManagementFactory.getRuntimeMXBean().getName(), {}.", t);
            }
        }

        // something like '<pid>@<hostname>', at least in SUN / Oracle JVMs
        int atIndex = value.indexOf('@');
        if (atIndex >= 0) {
            value = value.substring(0, atIndex);
        }

        int pid = -1;
        try {
            pid = Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            // value did not contain an integer
        }

        if (pid < 0 || pid > MAX_PROCESS_ID) {
            pid = ThreadLocalRandom.current().nextInt(MAX_PROCESS_ID + 1);

            logger.warn("Failed to find the current process ID from '{}'; using a random value: {}.",  value, pid);
        }

        return pid;
    }

    private static String getIP_16(String ip) {
        String[] segments = ip.split("\\.");
        StringBuilder buf = new StringBuilder();
        for (String s : segments) {
            String hex = Integer.toHexString(Integer.parseInt(s));
            if (hex.length() == 1) {
                buf.append('0');
            }
            buf.append(hex);
        }
        return buf.toString();
    }

    private static String getTraceId(String ip_16, long timestamp, long nextId) {
        StringBuilder buf = new StringBuilder()
                .append(ip_16)
                .append(timestamp)
                .append(nextId)
                .append(PID_FLAG)
                .append(PID);
        return buf.toString();
    }

    private static long getNextId() {
        // (1000 + 1) ~ (1000 + 8191)
        return (sequence.incrementAndGet() & ID_MASK) + ID_BASE;
    }
}
