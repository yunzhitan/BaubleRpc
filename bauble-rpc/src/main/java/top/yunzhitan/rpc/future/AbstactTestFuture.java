package top.yunzhitan.rpc.future;

import java.util.concurrent.CountDownLatch;

public class AbstactTestFuture {

    private Object result;

    private CountDownLatch downLatch = new CountDownLatch(1);
}
