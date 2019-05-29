package com.lichao.io;

import java.io.File;

/**
 * describe: 创建一个新文件
 *
 * @author lichao
 * @date 2019/01/01
 */
public class CreateFile {
    public static void main(String[] args) {
        File f = new File(System.getProperty("user.dir") + File.separator +"hello.txt");
        try{
            System.out.println(f.getAbsoluteFile());
            // File类的两个常量
            System.out.println(File.separator);
            System.out.println(File.pathSeparator);
            f.createNewFile();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
