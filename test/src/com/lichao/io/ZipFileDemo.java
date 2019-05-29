package com.lichao.io;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/01/01
 */
public class ZipFileDemo {
    public static void main(String[] args) throws IOException {
        /**
         * 解压缩文件（压缩文件中只有一个文件的情况）
         * */
        File file = new File(System.getProperty("user.dir") + File.separator + "hello.zip");
        File outFile = new File(System.getProperty("user.dir") + File.separator + "unZipFile.txt");
        // 用到一个ZipFile类.java中的每个压缩文件都是可以使用ZipFile进行表示的
        ZipFile zipFile = new ZipFile(file);
        ZipEntry entry = zipFile.getEntry("hello.txt");
        InputStream input = zipFile.getInputStream(entry);
        OutputStream output = new FileOutputStream(outFile);
        int temp;
        while((temp = input.read()) != -1){
            output.write(temp);
        }
        input.close();
        output.close();
    }
}
