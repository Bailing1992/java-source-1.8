package com.lichao.concurrent.threadPoolService;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/05/14
 */
public class ErrorTest {

    public static void main(String [] args){
        IntStream.range(1, 20000).forEach(
                (i) -> {
                    System.out.format("[thread:%s] executor start \n" , Thread.currentThread().getName());
                    ThreadPoolExecutor executor  = new ThreadPoolExecutor(
                            10, 20, 300, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<>());
                    IntStream.range(1, 20).forEach((j) -> executor.execute(() -> {
                            try {
                                System.out.format("[thread:%s] task start \n" , Thread.currentThread().getName());
                                Thread.sleep(30);
                                System.out.format("[thread:%s] task end \n" , Thread.currentThread().getName());
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    ));
                });

        System.out.format("[thread:%s] executor end \n", Thread.currentThread().getName());
    }

}
