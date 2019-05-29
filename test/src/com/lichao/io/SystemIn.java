package com.lichao.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/01/01
 */
public class SystemIn {

    public static void main(String[] args){
        File file = new File(System.getProperty("user.dir") + File.separator + "hello.txt");
        if(!file.exists()){
            return;
        }else{
            try{
                /**
                 * System.in重定向
                 * */
                System.setIn(new FileInputStream(file));
            }catch(FileNotFoundException e){
                e.printStackTrace();
            }
            byte[] bytes = new byte[1024];
            int len = 0;
            try{
                len = System.in.read(bytes);
            }catch(IOException e){
                e.printStackTrace();
            }
            System.out.println("读入的内容为：" + new String(bytes, 0, len));
        }
    }
}
