package com.lichao.io;

import java.io.IOException;
import java.io.OutputStream;

/**
 * describe:使用OutputStream向屏幕上输出内容
 *
 * @author lichao
 * @date 2019/01/01
 */
public class OutputStreamDemo {
    public static void main(String[] args) throws IOException {
        /**
         * 使用OutputStream向屏幕上输出内容
         * */
        OutputStream out = System.out;
        try{
            out.write("hello".getBytes());
        }catch (Exception e) {
            e.printStackTrace();
        }
        try{
            out.close();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
