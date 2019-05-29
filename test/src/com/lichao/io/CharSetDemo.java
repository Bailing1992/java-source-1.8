package com.lichao.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * describe:取得本地的默认编码
 *
 * @author lichao
 * @date 2019/01/01
 */
public class CharSetDemo {
    public static void main(String[] args) throws IOException {
        System.out.println("系统默认编码为：" + System.getProperty("file.encoding"));

        /**
         * 乱码的产生,一般情况下产生乱码，都是由于编码不一致的问题。
         * */
        File file = new File(System.getProperty("user.dir") + File.separator + "hello.txt");
        OutputStream out = new FileOutputStream(file);
        byte[] bytes = "你好".getBytes("ISO8859-1");
        out.write(bytes);
        out.close();
    }
}
