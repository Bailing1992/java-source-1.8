package com.lichao.io;

import java.io.*;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/01/01
 */
public class ExternalizableDemo {
    public static void main(String[] args) throws Exception{

        /**
         * 序列化和反序列化的操作
         * */
        ser(); // 序列化
        dser(); // 反序列化
    }


    public static void ser() throws Exception{
        File file = new File(System.getProperty("user.dir") + File.separator + "hello.txt");
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
        out.writeObject(new Person("rollen", 20));
        out.close();
    }


    public static void dser() throws Exception{
        File file = new File(System.getProperty("user.dir") + File.separator + "hello.txt");
        ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
        Object obj = input.readObject();
        input.close();
        System.out.println(obj);
    }


    static class Person implements Externalizable{
        public Person(){

        }

        public Person(String name, int age){
            this.name = name;
            this.age = age;
        }

        @Override
        public String toString(){
            return "姓名：" + name + "  年龄：" + age;
        }

        // 复写这个方法，根据需要可以保存的属性或者具体内容，在序列化的时候使用
        @Override
        public void writeExternal(ObjectOutput out) throws IOException{
            out.writeObject(this.name);
            out.writeInt(age);
        }

        // 复写这个方法，根据需要读取内容 反序列话的时候需要
        @Override
        public void readExternal(ObjectInput in) throws IOException,
                ClassNotFoundException{
            this.name = (String) in.readObject();
            this.age = in.readInt();
        }

        private String name;
        private int age;
    }

}


