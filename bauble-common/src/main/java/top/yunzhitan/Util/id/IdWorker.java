package top.yunzhitan.Util.id;

import top.yunzhitan.Util.SystemPropertyUtil;

/**
 * 使用twitter的snowflake算法生成分布式ID
 *
 *  * ***********************************************************************************************
 *                                          ID
 *  ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─
 *                      41                     │        8           │           14             │
 *  ├ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─
 *                                             │                    │                          │
 *  │                timestamp                          workerId                sequence
 *                                             │                    │                          │
 *  └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─

 */
public class IdWorker {

    private static IdWorker sington;

    private long workerId;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    private final long twepoch = 1288834974657L;

    private final long workerIdBits = 8L;
    private final long sequenceBits = 15L;
    private final long maxWorkerId = -1L ^ (-1L << workerIdBits);

    private final long workerIdShift = sequenceBits;
    private final long timestampLeftShift = sequenceBits + workerIdBits;
    private final long sequenceMask = -1L ^ (-1L << sequenceBits);

    public static IdWorker getInstance() {
        if(sington == null) {
            synchronized (IdWorker.class) {
                if(sington == null) {
                    sington = new IdWorker(SystemPropertyUtil.WORKER_ID);
                }
            }
        }
        return sington;
    }


    public IdWorker(long workerId) {
        if(workerId > maxWorkerId) {
            throw new IllegalArgumentException(String.format("workerId can't be greater than maxWorkerId"));
        }
        this.workerId = workerId;
    }

    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format("Runtime error"));
        }

        if(lastTimestamp == timestamp) {
            sequence = (sequence + 1) & sequenceMask;
            if(sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        }

        else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return ((timestamp-twepoch) << timestampLeftShift) |
                (workerId << workerIdShift) |
                sequence;
    }

    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

}
