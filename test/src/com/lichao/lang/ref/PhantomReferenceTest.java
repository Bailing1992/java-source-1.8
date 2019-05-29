package com.lichao.lang.ref;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

public class PhantomReferenceTest {


    public static void main(String [] args){

        Object obj = new Object();
        ReferenceQueue<Object> refQueue = new ReferenceQueue<Object>();
        PhantomReference<Object> phanRef = new PhantomReference<Object>(obj, refQueue);
        // 调用phanRef.get()不管在什么情况下会一直返回null
        System.out.println(phanRef.get());
        // 如果obj被置为null，当GC发现了虚引用，GC会将phanRef插入进我们之前创建时传入的refQueue队列
        // 注意，此时phanRef所引用的obj对象，并没有被GC回收，在我们显式地调用refQueue.poll返回phanRef之后
        // 当GC第二次发现虚引用，而此时JVM将phanRef插入到refQueue会插入失败，此时GC才会对obj进行回收
        System.out.println(refQueue.poll());
        obj = null;
        System.gc();

        System.out.println(phanRef.get());

        System.out.println(refQueue.poll());
        System.out.println(refQueue.poll());

    }



}
