package com.lichao.io;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/01/01
 */
public class FileWriterDemo {
    public static void main(String[] args) throws IOException {


        String fileName = System.getProperty("user.dir") + File.separator+"hello.txt";
        File f = new File(fileName);
        Writer out = new FileWriter(f);
        String str = "hello";
        out.write(str);
        out.close();


        //向文件中追加内容
        out = new FileWriter(f,true);
        str = "\r\nhello";
        out.write(str);
        out.close();
    }
}
