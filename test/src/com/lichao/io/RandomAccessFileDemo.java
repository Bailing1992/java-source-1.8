package com.lichao.io;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/01/01
 */
public class RandomAccessFileDemo {
    public static void main(String[] args) throws IOException {
        String fileName = System.getProperty("user.dir") + File.separator + "hello.txt";
        File f = new File(fileName);
        // 随意 访问
        RandomAccessFile demo = new RandomAccessFile(f, "rw");

        demo.writeBytes("asdsad");
        demo.writeInt(12);
        demo.writeBoolean(true);
        demo.writeChar('A');
        demo.writeFloat(1.21f);
        demo.writeDouble(12.123);
        demo.close();
    }
}
