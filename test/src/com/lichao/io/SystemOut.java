package com.lichao.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/01/01
 */
public class SystemOut {
    public static void main(String[] args){
        // 此刻直接输出到屏幕
        System.out.println("hello");

        File file = new File(System.getProperty("user.dir") + File.separator + "hello.txt");
        try{
            /**
             * 为System.out.println()重定向输出
             * */
            System.setOut(new PrintStream(new FileOutputStream(file)));
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        System.out.println("这些内容在文件中才能看到哦！");
    }
}
