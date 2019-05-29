package com.lichao.concurrent;

/**
 * describe:
 *
 * @author lichao
 * @date 2018/12/27
 */
public class VolatileDemo {

    private static volatile boolean isOver = false;

    public static void main(String[] args) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!isOver) ;
            }
        });
        thread.start();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        isOver = true;
    }

}
