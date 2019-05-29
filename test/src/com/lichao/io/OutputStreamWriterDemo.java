package com.lichao.io;

import java.io.*;

/**
 * describe: 将字节输出流转化为字符输出流
 *
 * @author lichao
 * @date 2019/01/01
 */
public class OutputStreamWriterDemo {

    public static void main(String[] args) throws IOException {
        String fileName = "d:" + File.separator + "hello.txt";
        File file = new File(fileName);
        // OutputStreramWriter将输出的字符流转化为字节流
        Writer out = new OutputStreamWriter(new FileOutputStream(file));
        out.write("hello");
        out.close();
    }
}
