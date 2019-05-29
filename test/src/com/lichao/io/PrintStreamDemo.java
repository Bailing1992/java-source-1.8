package com.lichao.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * describe:打印流: 使用PrintStream进行输出
 *
 * @author lichao
 * @date 2019/01/01
 */
public class PrintStreamDemo {
    public static void main(String[] args) throws IOException {
        /**
         * 使用PrintStream进行输出
         * 并进行格式化
         * */
        PrintStream print = new PrintStream(new FileOutputStream(new File(System.getProperty("user.dir")
                + File.separator + "hello.txt")));
        print.println(true);
        print.println("Rollen");
        String name = "Rollen";
        int age = 20;
        print.printf("姓名：%s. 年龄：%d.", name, age);
        print.close();
    }
}
