package com.lichao.lang.ref;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class WeakReferenceTest {

    /*
    * 由于System.gc()是告诉JVM这是一个执行GC的好时机，
    * 但具体执不执行由JVM决定，因此当JVM决定执行GC，
    * 得到的结果便是（事实上这段代码一般都会执行GC）：
    *
    * 从执行结果得知，通过调用weakRef.get()我们得到了obj对象，
    * 由于没有执行GC,因此refQueue.poll()返回的null，
    * 当我们把obj = null;此时没有引用指向堆中的obj对象了，
    * 这里JVM执行了一次GC，我们通过weakRef.get()发现返回了null，
    * 而refQueue.poll()返回了WeakReference对象，
    * 因此JVM在对obj进行了回收之后，才将weakRef插入到refQueue队列中。
    * */

    public static void main(String[] args) throws InterruptedException {
        Object obj = new Object();
        ReferenceQueue<Object> refQueue = new ReferenceQueue<>();
        WeakReference<Object> weakRef = new WeakReference<>(obj, refQueue);
        System.out.println(weakRef.get());
        System.out.println(refQueue.poll());
        obj = null;
        System.gc();
        System.out.println(weakRef.get());
        System.out.println(refQueue.poll());
    }

}
