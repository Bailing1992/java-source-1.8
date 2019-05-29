package com.lichao.io;

import java.io.*;

/**
 * describe:SequenceInputStream主要用来将2个流合并在一起，比如将两个txt中的内容合并为另外一个txt
 *
 * @author lichao
 * @date 2019/01/01
 */
public class SequenceInputStreamDemo {
    public static void main(String[] args) throws IOException {
        File file1 = new File(System.getProperty("user.dir") + File.separator + "hello1.txt");
        File file2 = new File(System.getProperty("user.dir") + File.separator + "hello2.txt");
        File file3 = new File(System.getProperty("user.dir") + File.separator + "hello.txt");
        InputStream input1 = new FileInputStream(file1);
        InputStream input2 = new FileInputStream(file2);
        OutputStream output = new FileOutputStream(file3);
        // 合并流
        /**
         * 将两个文本文件合并为另外一个文本文件
         * */
        SequenceInputStream sis = new SequenceInputStream(input1, input2);
        int temp;
        while((temp = sis.read()) != -1){
            output.write(temp);
        }
        input1.close();
        input2.close();
        output.close();
        sis.close();
    }
}
