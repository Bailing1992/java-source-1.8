package com.lichao.io;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * describe:文件压缩 ZipOutputStream类.如何压缩多个文件。
 *
 * @author lichao
 * @date 2019/01/01
 */
public class ZipOutputStreamDemo {
    public static void main(String[] args) throws IOException{
        // 要被压缩的文件夹
        File file = new File(System.getProperty("user.dir") + File.separator + "temp");
        File zipFile = new File(System.getProperty("user.dir")  + File.separator + "zipFile.zip");
        InputStream input;
        ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
        zipOut.setComment("hello");
        /**
         * 一次性压缩多个文件
         * */
        if(file.isDirectory()){
            File[] files = file.listFiles();
            for(int i = 0; i < files.length; ++i){
                input = new FileInputStream(files[i]);
                zipOut.putNextEntry(new ZipEntry(file.getName() + File.separator + files[i].getName()));
                int temp;
                while((temp = input.read()) != -1){
                    zipOut.write(temp);
                }
                input.close();
            }
        }
        zipOut.close();
    }
}
