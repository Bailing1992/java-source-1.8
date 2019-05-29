package com.lichao.io;

import java.io.File;

/**
 * describe:删除一个文件
 *
 * @author lichao
 * @date 2019/01/01
 */
public class DeleteFile {
    public static void main(String[] args) {
        String fileName = System.getProperty("user.dir") + File.separator + "hello.txt";
        File f = new File(fileName);
        if(f.exists()){
            f.delete();
        }else{
            System.out.println("文件不存在");
        }

    }
}
