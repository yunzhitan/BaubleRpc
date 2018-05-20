package top.yunzhitan.Util;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * 利用对象继承的内存布局规则来padding避免false sharing, 注意其中对象头会至少占用8个字节
 * ---------------------------------------
 *  For 32 bit JVM:
 *      _mark   : 4 byte constant
 *      _klass  : 4 byte pointer to class
 *  For 64 bit JVM:
 *      _mark   : 8 byte constant
 *      _klass  : 8 byte pointer to class
 *  For 64 bit JVM with compressed-oops:
 *      _mark   : 8 byte constant
 *      _klass  : 4 byte pointer to class
 * ---------------------------------------
 */
class IntLhsPadding {
    @SuppressWarnings("unused")
    protected long p01, p02, p03, p04, p05, p06, p07;
}

class IntValue extends IntLhsPadding {
    protected volatile int value;
}

class IntRhsPadding extends IntValue {
    @SuppressWarnings("unused")
    protected long p09, p10, p11, p12, p13, p14, p15;
}

/**
 * 序号生成器, 每个线程预先申请一个区间, 步长(step)固定, 以此种方式尽量减少CAS操作,
 * 需要注意的是, 这个序号生成器不是严格自增的, 并且也溢出也是可以接受的(接受负数).
 *
 * jupiter
 * org.jupiter.common.util
 *
 * @author jiachun.fjc
 */
public class IntSequence extends IntRhsPadding {

    private static final int DEFAULT_STEP = 64;

    private static final AtomicIntegerFieldUpdater<IntValue> updater = AtomicIntegerFieldUpdater.newUpdater(IntValue.class, "value");

    private final ThreadLocal<LocalSequence> localSequence = new ThreadLocal<LocalSequence>() {

        @Override
        protected LocalSequence initialValue() {
            return new LocalSequence();
        }
    };

    private final int step;

    public IntSequence() {
        this(DEFAULT_STEP);
    }

    public IntSequence(int step) {
        this.step = step;
    }

    public IntSequence(int initialValue, int step) {
        updater.set(this, initialValue);
        this.step = step;
    }

    public int next() {
        return localSequence.get().next();
    }

    private int getNextBaseValue() {
        return updater.getAndAdd(this, step);
    }

    private final class LocalSequence {

        private int localBase = getNextBaseValue();
        private int localValue = 0;

        public int next() {
            int realVal = ++localValue + localBase;

            if (localValue == step) {
                localBase = getNextBaseValue();
                localValue = 0;
            }

            return realVal;
        }
    }
}

