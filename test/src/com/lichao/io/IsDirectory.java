package com.lichao.io;

import java.io.File;

/**
 * describe:判断一个指定的路径是否为目录
 *
 * @author lichao
 * @date 2019/01/01
 */
public class IsDirectory {
    public static void main(String[] args) {
        String fileName = System.getProperty("user.dir") + File.separator;

        /**
         * 使用isDirectory判断一个指定的路径是否为目录
         * */
        File f = new File(fileName);


        if(f.isDirectory()){
            System.out.println("YES");
        }else{
            System.out.println("NO");
        }
    }
}
