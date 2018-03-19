package top.yunzhitan.transport;

import top.yunzhitan.Util.NetUtil;
import top.yunzhitan.rpc.tracing.TraceId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 链路追踪ID生成工具
 * 1  ~ 13 位： 时间戳
 * 14  ~ 21 位: 本地workerID
 * 22 ~ 48 位: 本地自增ID
 * 49 ~ 64 位: 当前进程ID(16进制)

 */
public class TraceUtil {

    private static final Logger logger = LoggerFactory.getLogger(TraceUtil.class);
    private ThreadLocal<TraceId> traceIdThreadLocal = new ThreadLocal<>();
    private final long twepoch = 1288834974657L;
    private static final long workerIdBits = 8L;
    private static final long pidBits = 16L;
    private static final long sequenceBits = 20L;
    private static final long timestempShift = workerIdBits + pidBits + sequenceBits;
    private static final long workerIdShift = sequenceBits + pidBits;
    private static final long sequenceShift = pidBits;

    private static final long MAX_PROCESS_ID = -1L << (-1L << pidBits);
    private static final long MAX_WORKER_ID = -1L << (-1L << workerIdBits);
    private static final long MAX_SEQUENCE_ID = -1L << (-1L << sequenceBits);
    private static final long workerID;
    private static final long pid;
    private static final AtomicLong seqence = new AtomicLong();

    static {
        String ip_16;
        try {
            ip_16 = SystemPropertyUtil.get("bauble.local.address", NetUtil.getLocalAddress());
        } catch (Throwable t) {
            ip_16 = "fffffff";
        }
        IP = getIP_16(ip_16);

        String pid_16;
    }

    private static int getProcessId() {
        String value = "";
        try {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            value = runtimeMXBean.getName();
        } catch (Throwable t) {
            logger.debug("Could not invoke ManagementFactory.getRuntimeMXBean().getName(),{}",t);
        }
        //value: pid@hostname
        int atIndex = value.indexOf('@');
        if (atIndex >= 0) {
            value = value.substring(0,atIndex);
        }
        int pid = -1;
        pid = Integer.parseInt(value);

        if (pid < 0 || pid > MAX_PROCESS_ID) {
            pid = ThreadLocalRandom.current().nextInt(MAX_PROCESS_ID + 1);
        }

        return pid;
    }

    private static int getIP_16(String ipString) {
            int[] ip = new int[4];
            int pos1= ipString.indexOf(".");
            int pos2= ipString.indexOf(".",pos1+1);
            int pos3= ipString.indexOf(".",pos2+1);
            ip[0] = Integer.parseInt(ipString.substring(0 , pos1));
            ip[1] = Integer.parseInt(ipString.substring(pos1+1 , pos2));
            ip[2] = Integer.parseInt(ipString.substring(pos2+1 , pos3));
            ip[3] = Integer.parseInt(ipString.substring(pos3+1));
            return (ip[0]<<24)+(ip[1]<<16)+(ip[2]<<8)+ip[3];
    }

}
