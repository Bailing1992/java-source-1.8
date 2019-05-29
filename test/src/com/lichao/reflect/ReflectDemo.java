package com.lichao.reflect;

import com.lichao.concurrent.CreateThreadDemo;

/**
 * describe:
 *
 * @author lichao
 * @date 2018/12/29
 */
public class ReflectDemo {

    public static void main(String[] args) throws Exception {

        /**
         *
         * describe:实例化Class类对象
         *
         */
        Class<?> class1;
        Class<?> class2;
        Class<?> class3;
        // 一般采用这种形式
        class1 = Class.forName("com.lichao.concurrent.CreateThreadDemo");
        class2 = new CreateThreadDemo().getClass();
        class3 = CreateThreadDemo.class;
        System.out.println("类名称   " + class1.getName());
        System.out.println("类名称   " + class2.getName());
        System.out.println("类名称   " + class3.getName());
    }
}
