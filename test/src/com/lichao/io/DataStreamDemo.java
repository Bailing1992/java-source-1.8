package com.lichao.io;

import java.io.*;

/**
 * describe:数据操作流 DataOutputStream、DataInputStream类
 *
 * @author lichao
 * @date 2019/01/01
 */
public class DataStreamDemo {
    public static void main(String[] args) throws IOException {
        File file = new File("d:" + File.separator + "hello.txt");
        char[] ch = { 'A', 'B', 'C' };
        DataOutputStream out;
        out = new DataOutputStream(new FileOutputStream(file));
        for(char temp : ch){
            out.writeChar(temp);
        }
        out.close();

        // 使用DataInputStream读出内容
        DataInputStream input = new DataInputStream(new FileInputStream(file));
        ch = new char[10];
        int count = 0;
        char temp;
        while((temp = input.readChar()) != 'C'){
            ch[count++] = temp;
        }
        System.out.println(ch);
    }
}
