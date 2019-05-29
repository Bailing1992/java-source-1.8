package com.lichao.lang.ref.PhantomReference;

import java.io.Closeable;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.List;

public class ResourceCloseDeamon extends Thread {

    //
    private static ReferenceQueue QUEUE = new ReferenceQueue();

    // 保持对reference的引用, 防止reference本身被回收
    private static List<Reference> references = new ArrayList<>();
    @Override
    public void run() {
        this.setName( "ResourceCloseDeamon");
        while (true) {
            try {
                // ReferenceQueue 队列中-获取的是ResourcePhantomReference 引用的对象；
                ResourcePhantomReference reference = (ResourcePhantomReference) QUEUE.remove();
                // 执行的是包装类中的方法，operation已经被gcc清除；
                reference.cleanUp();
                // 删除包装类的引用；
                references.remove(reference);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void register(Object operation, List<Closeable> closeables) {
        // references添加的是 ResourcePhantomReference的引用，而不是operation本身，ResourcePhantomReference是Operation的包装类；
        references.add(new ResourcePhantomReference(operation, QUEUE, closeables));
    }
}