package com.lichao.concurrent.threadPoolService;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/05/14
 */
public class MyThreadPoolService {

    private MyThreadPoolService(){}

    private static volatile ThreadPoolExecutor single;


    public static ThreadPoolExecutor getService(){
        if(single == null){
            synchronized (MyThreadPoolService.class){
                if(single == null){
                    single = new ThreadPoolExecutor(
                            10, 20, 300, TimeUnit.MICROSECONDS, new LinkedBlockingDeque<>());
                }
            }
        }
        return single;
    }
}
