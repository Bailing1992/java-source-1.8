package com.lichao.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;

/**
 * describe: spliterator.trySplit()的执行存在并发问题:
 *     若 Index=0, fence=50
 *       Index=50, fence=75
 *       Index=75, fence=86
 *       Index=86, fence=93
 *             导致93至100没有处理
 *
 *      另外，spliterator.trySplit().forEachRemaining(new Consumer()）
 * 若已有线程在迭代执行任务时，其他的线程进行拆分任务，导致部分任务重复执行；
 *
 *
 * @author lichao
 * @date 2018/12/26
 */
public class SpliteratorDemo {

    static AtomicInteger count = new AtomicInteger(0);
    static List<String> strList = createList();
    static Spliterator spliterator = strList.spliterator();

    /**
     * 多线程计算list中数值的和
     * 测试 spliterator 遍历
     */
    public static void main(String [] args){
        String threadName = Thread.currentThread().getName();
        System.out.println("注线程"+threadName+"开始运行-----");
        for(int i=0;i<4;i++){
            new MyThread().start();
        }
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("结果为：" + count);
    }

    static class MyThread extends Thread{
        @Override
        public void run() {
            String threadName = Thread.currentThread().getName();
            System.out.println("线程"+threadName+"开始运行-----");
            spliterator.trySplit().forEachRemaining(new Consumer() {
                @Override
                public void accept(Object o) {
                    if(isInteger((String)o)){
                        int num = Integer.parseInt(o +"");
                        count.addAndGet(num);
                        System.out.println("数值："+num+"------"+threadName);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            System.out.println("线程"+threadName+"运行结束-----");
        }
    }

    private static List<String> createList(){
        List<String> result = new ArrayList<>();
        for(int i=0; i<100; i++){
            if(i % 10 == 0){
                result.add(i+"");
            }else{
                result.add("aaa");
            }
        }
        return result;
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }
}