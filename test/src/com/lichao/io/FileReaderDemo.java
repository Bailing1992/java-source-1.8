package com.lichao.io;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/01/01
 */
public class FileReaderDemo {
    public static void main(String[] args) throws IOException {
        String fileName = System.getProperty("user.dir") + File.separator+"hello.txt";
        File f = new File(fileName);
        char[] ch = new char[100];
        Reader read = new FileReader(f);
        int count = read.read(ch);
        read.close();
        System.out.println("读入的长度为:"+count);
        System.out.println("内容为:" + new String(ch,0, count));


        //最好采用循环读取的方式，因为我们有时候不知道文件到底有多大
        fileName = System.getProperty("user.dir") + File.separator + "hello.txt";
        f = new File(fileName);
        ch = new char[100];
        read = new FileReader(f);
        int temp;
        count = 0;
        while((temp = read.read()) != -1){
            ch[count++] = (char)temp;
        }
        read.close();
        System.out.println("内容为:" + new String(ch,0, count));
    }
}
