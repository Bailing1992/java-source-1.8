package com.lichao.java8;

/**
 * describe:
 *
 * @author lichao
 * @date 2018/12/28
 */
interface InterfaceA {


    default void foo() {
        System.out.println("InterfaceA foo");
    }


    class ClassA implements InterfaceA {
    }


    class Test {
        public static void main(String[] args) {
            new ClassA().foo(); // 打印：“InterfaceA foo”
        }
    }
}
