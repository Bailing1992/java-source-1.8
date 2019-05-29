package com.lichao.io;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * describe:
 *
 * @author lichao
 * @date 2019/01/01
 */
public class ZipFileDemo2 {

    public static void main(String[] args) throws IOException {

        File file = new File(System.getProperty("user.dir") + File.separator + "zipFile.zip");
        File outFile;
        ZipFile zipFile = new ZipFile(file);
        ZipInputStream zipInput = new ZipInputStream(new FileInputStream(file));
        ZipEntry entry;
        InputStream input;
        OutputStream output;
        while((entry = zipInput.getNextEntry()) != null){
            System.out.println("解压缩" + entry.getName() + "文件");
            outFile = new File(System.getProperty("user.dir") + File.separator + entry.getName());
            if(!outFile.getParentFile().exists()){
                outFile.getParentFile().mkdir();
            }
            if(!outFile.exists()){
                outFile.createNewFile();
            }
            input = zipFile.getInputStream(entry);
            output = new FileOutputStream(outFile);
            int temp;
            while((temp = input.read()) != -1){
                output.write(temp);
            }
            input.close();
            output.close();
        }
    }
}
