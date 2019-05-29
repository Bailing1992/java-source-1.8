package com.lichao.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

/**
 * describe: 回退流操作
 *
 * @author lichao
 * @date 2019/01/01
 */
public class PushBackInputStreamDemo {

    public static void main(String[] args) throws IOException {
        String str = "hello,rollenholt";
        PushbackInputStream push;
        ByteArrayInputStream bat;
        bat = new ByteArrayInputStream(str.getBytes());
        push = new PushbackInputStream(bat);
        int temp;
        while((temp = push.read()) != -1){
            if(temp == ','){
                push.unread(temp);
                temp = push.read();
                System.out.print("(回退" + (char) temp + ") ");
            }else{
                System.out.print((char) temp);
            }
        }
    }
}
