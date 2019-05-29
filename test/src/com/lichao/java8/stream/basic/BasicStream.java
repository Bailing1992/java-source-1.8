package com.lichao.java8.stream.basic;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/05/06
 */
public class BasicStream {

    public static void main(String [] args){
        IntStream.range(1, 4).forEach(System.out::println);

        Arrays
                .stream(new int [] {1, 2, 3})
                .map(n -> 2 * n + 1)
                .average()
                .ifPresent(System.out::println);
        Stream.of("d2", "f2", "b1", "b3", "c")
                .filter(s -> {
                    System.out.println("filter" + s);
                    return true;
                })
                .forEach(s -> System.out.println("forEach" + s));


        Stream.of("d2", "f2", "b1", "b3", "c")
                .map(s -> {
                    System.out.println("map: " + s);
                    return s.toUpperCase(); })
                .anyMatch(s -> {
                    System.out.println("anyMatch:" + s);
                    return s.startsWith("F"); });



    }
}
