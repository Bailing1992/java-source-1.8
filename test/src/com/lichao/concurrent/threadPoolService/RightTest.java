package com.lichao.concurrent.threadPoolService;

import java.util.stream.IntStream;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/05/14
 */
public class RightTest {


    public static void main(String [] args){

        IntStream.range(1, 200).forEach(
                (i) -> MyThreadPoolService.getService().execute(() -> {
                        try {
                            Thread.sleep(300);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    })
                );
    }
}
