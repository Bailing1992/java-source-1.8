package com.lichao.io;

import java.io.File;

/**
 * describe:列出指定目录的全部文件（包括隐藏文件）
 *
 * @author lichao
 * @date 2019/01/01
 */
public class ListFile {
    public static void main(String[] args) {


        String fileName = System.getProperty("user.dir") + File.separator;
        File f = new File(fileName);

        /**
         * 使用list列出指定目录的全部文件
         * */
        String[] str = f.list();
        for (int i = 0; i < str.length; i++) {
            System.out.println(str[i]);
        }


        /**
         * 使用listFiles列出指定目录的全部文件
         * listFiles输出的是完整路径
         * */
        File[] files = f.listFiles();
        for (int i = 0; i < files.length; i++) {
            System.out.println(files[i]);
        }
    }
}
