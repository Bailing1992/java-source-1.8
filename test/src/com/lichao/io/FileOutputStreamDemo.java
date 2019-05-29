package com.lichao.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/01/01
 */
public class FileOutputStreamDemo {
    public static void main(String[] args) throws IOException {

        String fileName = System.getProperty("user.dir") + File.separator + "hello.txt";
        File f = new File(fileName);
        OutputStream out = new FileOutputStream(f);

        /**
         * 字节流
         * 向文件中写入字符串
         * */
        String str = "你好";
        byte[] b = str.getBytes();
        out.write(b);

        for (int i = 0; i < b.length; i++) {
            out.write(b[i]);
        }

        out.close();


        /**
         * 字节流
         * 向文件中追加新内容：
         * */
        fileName = System.getProperty("user.dir") + File.separator + "hello.txt";
        f = new File(fileName);
        out =new FileOutputStream(f,true);
        str = "Rollen";
        b = str.getBytes();
        for (int i = 0; i < b.length; i++) {
            out.write(b[i]);
        }
        out.close();


    }
}
