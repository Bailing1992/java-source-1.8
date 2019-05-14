package javaTest.lang.ref;

import java.util.Map;
import java.util.WeakHashMap;


// WeakReference应用场景，JDK自带的WeakHashMap，
// 我们用下面的代码来测试查看WeakHashMap在gc后的entry的情况，加入-verbose:gc运行。


public class GCTest {

    private static Map<String,byte[]> caches=new WeakHashMap<>();

    public static void main(String[]args) throws InterruptedException {
        for (int i=0;i<100000;i++){
            caches.put(i+"",new byte[1024*1024*10]);
            System.out.println("put num: " + i +  "but caches size:" + caches.size());
        }
    }
}
