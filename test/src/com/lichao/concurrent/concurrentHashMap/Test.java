package com.lichao.concurrent.concurrentHashMap;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: lichao
 * @Date: 2019-05-29 08:23
 */
public class Test {

    public static void main(String [] args){
        ConcurrentHashMap<Integer, Integer> concurrentHashMap =new ConcurrentHashMap<>();

        for(int ii = 0; ii < 1000; ii++){
            for(int JJ = 0; JJ < 100; JJ++) {
                concurrentHashMap.put(1, ii+JJ);
            }
        }
    }
}
