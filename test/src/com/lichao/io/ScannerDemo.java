package com.lichao.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * describe:采用Scanner类来进行数据输入；Scanner可以接受任何的输入流
 *
 * @author lichao
 * @date 2019/01/01
 */
public class ScannerDemo {
    public static void main(String[] args){

        Scanner sca = new Scanner(System.in);
        // 读一个整数
        int temp = sca.nextInt();
        System.out.println(temp);
        //读取浮点数
        float flo = sca.nextFloat();
        System.out.println(flo);


        /**
         * Scanner的小例子，从文件中读内容
         * */
        File file = new File(System.getProperty("user.dir") + File.separator + "hello.txt");
        try{
            sca = new Scanner(file);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }
        String str = sca.next();
        System.out.println("从文件中读取的内容是：" + str);
    }
}
