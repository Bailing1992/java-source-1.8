package com.lichao.io;

import java.io.*;

/**
 * describe: 将字节输入流变为字符输入流
 *
 * @author lichao
 * @date 2019/01/01
 */
public class InputStreamReaderDemo {
    public static void main(String[] args) throws IOException {
        String fileName = System.getProperty("user.dir") + File.separator + "hello.txt";
        File file = new File(fileName);
        //InputStreamReader将输入的字节流转换为字符流
        Reader read = new InputStreamReader(new FileInputStream(file));
        char[] b = new char[100];
        int len = read.read(b);
        System.out.println(new String(b,0,len));
        read.close();
    }
}
