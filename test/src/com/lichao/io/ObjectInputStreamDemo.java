
package com.lichao.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

/**
 * describe:
 *  其实只有属性会被序列化。
 * @author lichao
 * @date 2019/01/01
 */
public class ObjectInputStreamDemo {
    public static void main(String[] args) throws Exception{
        File file = new File(System.getProperty("user.dir") + File.separator + "hello.txt");
        ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
        /**
         * ObjectInputStream示范
         * */
        Object obj = input.readObject();
        input.close();
        System.out.println(obj);
    }
}
