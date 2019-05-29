package com.lichao.io;

import java.io.*;

/**
 * describe:对象序列化=把一个对象-变为二进制数据流的一种方法。
 * 一个类要想被序列化，就必须实现java.io.Serializable接口。
 * 虽然这个接口中没有任何方法，就如同之前的cloneable接口一样。
 * 实现了这个接口之后，就表示这个类具有被序列化的能力。
 *
 * @author lichao
 * @date 2019/01/01
 */
public class ObjectOutputStreamDemo {

    public static void main(String[] args) throws IOException{
        File file = new File(System.getProperty("user.dir") + File.separator + "hello.txt");
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
        oos.writeObject(new Person("rollen", 20));
        oos.close();
    }
}

/**
 * 实现具有序列化能力的类
 * */
class Person implements Serializable {

    public Person(String name, int age){
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString(){
        return "姓名：" + name + "  年龄：" + age;
    }

    private String name;
    private int age;
}