package com.lichao.concurrent.semaphore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/05/17
 */
public class TIJ_semaphore {

    public static void main(String [] args){
        ExecutorService executorService = Executors.newCachedThreadPool();
        final Semaphore semaphore = new Semaphore(5);


        for ( int index = 0; index < 20; index ++) {
            final int NO = index;
            Runnable run = () -> {

                    try {
                        semaphore.acquire();
                        System.out.println("Accessing " + NO);
                        Thread.sleep(10000l);
                        semaphore.release();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            };
            executorService.execute(run);
        }
    }
}
