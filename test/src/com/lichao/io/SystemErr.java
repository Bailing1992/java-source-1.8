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
public class SystemErr {
    public static void main(String[] args){

        File file = new File(System.getProperty("user.dir") + File.separator + "hello.txt");
        /**
         * System.err 重定向 这个例子也提示我们可以使用这种方法保存错误信息
         * */
        System.err.println("这些在控制台输出");
        try{
            System.setErr(new PrintStream(new FileOutputStream(file)));
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        System.err.println("这些在文件中才能看到哦！");
    }
}
