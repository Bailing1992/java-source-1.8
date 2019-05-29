package com.lichao.jvm;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/05/15
 */
public class FinalizableImpl {
    private static AtomicInteger aliveCount = new AtomicInteger(0);


    FinalizableImpl() {
        aliveCount.incrementAndGet();
    }


    @Override
    protected void finalize() throws Throwable {
        FinalizableImpl.aliveCount.decrementAndGet();
    }

    public static void main(String args[]) {
        for (int i = 0;; i++) {
            FinalizableImpl f = new FinalizableImpl();
            if ((i % 100_000) == 0) {
                System.out.format("After creating %d objects, %d are still alive.%n", new Object[] {i, FinalizableImpl.aliveCount.get() });
            }
        }
    }
}
