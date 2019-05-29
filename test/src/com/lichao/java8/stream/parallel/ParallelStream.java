package com.lichao.java8.stream.parallel;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

/**
 * describe: 并行流使用了所有公共的ForkJoinPool中的可用线程来执行流式操作；
 *
 * @author lichao
 * @date 2019/05/06
 */
public class ParallelStream {


    public static void main(String [] args){
        ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
        System.out.println(forkJoinPool.getParallelism());


        Arrays.asList("a1", "a2", "b1", "c2", "c1")
                .parallelStream().filter(s -> {
                    System.out.format("filter: %s [%s]\n", s, Thread.currentThread().getName());
                    return true; })
                .map(s -> {
                    System.out.format("map: %s [%s]\n", s, Thread.currentThread().getName());
                    return s.toUpperCase();})
                .forEach(s -> System.out.format("forEach: %s [%s]\n", s, Thread.currentThread().getName()));


        System.out.println();

        Arrays.asList("a1", "a2", "b1", "c2", "c1")
                .parallelStream()
                .filter(s -> {
                    System.out.format("filter: %s [%s]\n", s, Thread.currentThread().getName());
                    return true; })
                .map(s -> {
                    System.out.format("map: %s [%s]\n", s, Thread.currentThread().getName());
                    return s.toUpperCase();})
                .sorted((s1,s2) ->{
                    System.out.format("sorted: %s <> %s [%s]\n", s1, s2, Thread.currentThread().getName());
                    return s1.compareTo(s2); })
                .forEach(s -> System.out.format("forEach: %s [%s]\n", s, Thread.currentThread().getName()));

        Person [] persons = new Person[]{
                new Person(1,"A"),
                new Person(2,"B"),
                new Person(3,"C"),
                new Person(4,"D")
        };
        Person result = Arrays.asList(persons).stream().reduce(new Person(0, ""), (p1, p2) -> {
            p1.age += p2.age;
            p1.name += p2.name;
            return p1;
        });

        System.out.format("\nresult:[ age:%s name:%s ]\n", result.age,result.name);

        System.out.println();

        int ret = Arrays.asList(persons)
                .stream()
                .reduce(0, (sum, p2)-> {
                    System.out.format("accumulator sum:%s person: %s\n", sum, p2.name);
                    return sum + p2.age;
                    }, (sum1, sum2) -> {
                    System.out.format("combiner sum1:%s sum2: %s\n", sum1, sum2);
                    return sum1 + sum2;});
        System.out.format("\nret:[ age:%s ]\n", ret);

        System.out.println();

        int ret2 = Arrays.asList(persons)
                .parallelStream()
                .reduce(0, (sum, p2)-> {
                    System.out.format("accumulator sum:%s person: %s [%s]\n", sum, p2.name, Thread.currentThread().getName());
                    return sum + p2.age;
                }, (sum1, sum2) -> {
                    System.out.format("combiner sum1:%s sum2: %s [%s] \n", sum1, sum2, Thread.currentThread().getName());
                    return sum1 + sum2;});
        System.out.format("\n ret:[ age:%s ]\n", ret2);


    }



    static class Person{
        int age;
        String name;
        Person(int age, String name ){
            this.age = age;
            this.name = name;
        }
    }


}
