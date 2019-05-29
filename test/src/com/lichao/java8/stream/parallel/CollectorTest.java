package com.lichao.java8.stream.parallel;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collector;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/05/06
 */
public class CollectorTest {


    public static void main(String [] args){
        Collector<Person, StringJoiner, String> personNameCollector = Collector.of(
                () -> new StringJoiner("|"),
                (j, p) -> j.add(p.name.toUpperCase()),
                (j1, j2) -> j1.merge(j2),
                StringJoiner::toString);

        Person[] persons = new Person[]{
                new Person(1,"A"),
                new Person(2,"B"),
                new Person(3,"C"),
                new Person(4,"D")
        };
        String name = Arrays.asList(persons).stream().collect(personNameCollector);
    }

    static class Person{
        int age;
        String name;
        String getName(){
            return name;
        }
        Person(int age, String name ){
            this.age = age;
            this.name = name;
        }
    }
}
