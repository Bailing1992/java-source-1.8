package com.lichao.io;

import java.io.File;

/**
 * describe:搜索指定目录的全部内容
 *
 * @author lichao
 * @date 2019/01/01
 */
public class ListAll {
    public static void main(String[] args) {
        String fileName = System.getProperty("user.dir") + File.separator;
        File f = new File(fileName);
        print(f);
    }
    public static void print(File f){
        if(f != null){
            if(f.isDirectory()){
                File[] fileArray = f.listFiles();
                if(fileArray != null){
                    for (int i = 0; i < fileArray.length; i++) {
                        //递归调用
                        print(fileArray[i]);
                    }
                }
            }
            else{
                System.out.println(f);
            }
        }
    }
}
