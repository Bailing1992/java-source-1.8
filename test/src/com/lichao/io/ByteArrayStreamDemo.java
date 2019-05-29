package com.lichao.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * describe:以内存为输出输入目的地，使用内存操作流
 *
 * @author lichao
 * @date 2019/01/01
 */
public class ByteArrayStreamDemo {
    public static void main(String[] args) throws IOException {
        String str = "ni";
        // 主要将内容写入内容
        ByteArrayInputStream input = new ByteArrayInputStream(str.getBytes());
        // 主要将内容从内存输出
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int temp;
        while((temp = input.read()) != -1){
            char ch = (char)temp;
            output.write(Character.toLowerCase(ch));
        }
        String outStr = output.toString();
        input.close();
        output.close();
        System.out.println(outStr);
    }
}
