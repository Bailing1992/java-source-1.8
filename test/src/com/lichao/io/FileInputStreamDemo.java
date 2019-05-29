package com.lichao.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * describe:读取文件内容
 *
 * @author lichao
 * @date 2019/01/01
 */
public class FileInputStreamDemo {
    public static void main(String[] args) throws IOException {
        String fileName = System.getProperty("user.dir") + File.separator + "hello.txt";
        File f = new File(fileName);
        InputStream in = new FileInputStream(f);
        byte[] b = new byte[1024];
        int len = in.read(b);
        in.close();
        System.out.println("文件长度为："+len + "\n" + new String(b,0,len));



        /**
         * 字节流
         * 读文件内容,节省空间
         * */

        fileName = System.getProperty("user.dir") + File.separator + "hello.txt";
        f =new File(fileName);
        in = new FileInputStream(f);
        b = new byte[(int)f.length()];
        in.read(b);
        System.out.println("文件长度为："+f.length());
        in.close();
        System.out.println(new String(b));


        /**
         * 字节流
         * 读文件内容,节省空间
         * */
        fileName = System.getProperty("user.dir") + File.separator + "hello.txt";
        f = new File(fileName);
        in = new FileInputStream(f);
        b = new byte[(int)f.length()];
        for (int i = 0; i < b.length; i++) {
            b[i]=(byte)in.read();
        }
        in.close();
        System.out.println(new String(b));



        fileName = System.getProperty("user.dir") + File.separator + "hello.txt";
        f = new File(fileName);
        in = new FileInputStream(f);
        b = new byte[1024];
        int count =0;
        int temp;
        //判断是否独到文件的末尾
        //唯独到文件末尾的时候会返回-1.正常情况下是不会返回-1的
        while((temp = in.read()) != -1){
            b[count++] = (byte) temp;
        }
        in.close();
        System.out.println(new String(b,0, count));
    }
}
