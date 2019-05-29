package com.lichao.io;

import java.io.File;

/**
 * describe:创建一个文件夹
 *
 * @author lichao
 * @date 2019/01/01
 */
public class Mkdir {
    public static void main(String[] args) {
        String fileName = System.getProperty("user.dir") + File.separator + "hello";
        File f = new File(fileName);
        f.mkdir();
    }
}
