package javaTest.lang.ref;

import java.util.WeakHashMap;

public class WeakHashMapTest {
    public static void main(String[] args) {
        WeakHashMap w= new WeakHashMap();
        //三个key-value中的key 都是匿名对象，没有强引用指向该实际对象
        w.put(new String("语文"),new String("优秀"));
        w.put(new String("数学"), new String("及格"));
        w.put(new String("英语"), new String("中等"));
        //增加一个字符串的强引用
        w.put("java", new String("特别优秀"));
        System.out.println(w);
        //通知垃圾回收机制来进行回收
        System.gc();
        System.runFinalization();
        //再次输出w
        System.out.println("第二次输出:"+w);
    }
}
