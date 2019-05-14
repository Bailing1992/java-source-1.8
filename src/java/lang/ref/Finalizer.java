/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.

 * 因为jvm只能管理jvm内存空间(堆)，但是对于应用运行时需要的其它native资源（堆外内存）(jvm通过jni暴漏出来的功能)：
 * 例如直接内存DirectByteBuffer，网络连接SocksSocketImpl，文件流FileInputStream等与操作系统有交互的资源，
 * jvm就无能为力了，需要我们自己来调用释放这些资源方法来释放，为了避免对象死了之后，程序员忘记手动释放这些资源，
 * 导致这些对象有的外部资源泄露，java提供了finalizer机制通过重写对象的finalizer方法，在这个方法里面执行释放对象占用的外部资源的操作，
 * 这样使用这些资源的程序员即使忘记手动释放，jvm也可以在回收对象之前帮助释放掉这些外部资源，
 * 帮助我们调用这个方法回收资源的线程就是我们在导出jvm线程栈时看到的名为Finalizer的守护线程；
 *
 *
 *
 *
 *
 * 什么样的对象会被封装为Finalizer对象？
 *
 * 1.  当前类或其父类含有一个参数为空，返回值为void，名为finalize的方法；
 * 2.  这个finalize方法体不能为空；
 *
 * 满足以上条件的类称之为f类
 *
 * f类的对象是如何被封装为Finalizer对象的？
 * Finalizer类的两个关键的方法：
 *
 *
 */

package java.lang.ref;

import java.security.PrivilegedAction;
import java.security.AccessController;
import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;
import sun.misc.VM;


// Finalizer继承FinalReference类，FinalReference继承Reference类，
// 对象最终会被封装为Finalizer对象，如果去查看源码会发现Finalizer的构造方法是不对外暴漏，所以我们无法自己创建Finalizer对象，FinalReference是由jvm自动封装；


// Finalizer继承FinalReference类，FinalReference继承Reference类，对象最终会被封装为Finalizer对象；
// FinalReference是由jvm自动封装；
final class Finalizer extends FinalReference<Object> { /* Package-private; must be in
                                                          same package as the Reference
                                                          class */

    // ReferenceQueue队列,传说中的f-queue队列，这个队列是全局唯一的，
    // 当gc线程发现f类对象除了Finalizer引用外，没有强引用了，就会把它放入到pending队列，
    // HanderReference线程在pending队列取到FinalReference对象的时候，会将把他们都放到这个f-queue队列里面，
    // 然后Finalizer线程就可以去这个队列里取出Finalizer对象，在将其移出unfinized队列，最后调用f类的finalizer方法；

    private static ReferenceQueue<Object> queue = new ReferenceQueue<>();
    //  它是unfialized队列的队头，这个队列里面存了Finalizer对象，这里面的Finalizer对象引用的f类都还没有执行finalized方法，
    // unfialized与next、prev三个成员变量组成一个双向链表的数据结构，unfialized永远指向这个链表的头，
    // 在Finalizer对象创建的时候会加入到此队列头部，它是静态全局唯一的，所以在这个链表里面的对象都是无法被回收的；
    // 在执行f类的finalizer之前，会将引用它的Finalizer对象从unfinalized队列里移除；
    // 这个队列的作用是保存全部的只存在FinalizerReference引用、且没有执行过finalize方法的f类的Finalizer对象，
    // 防止finalizer对象在其引用的对象之前被gc回收掉；

    // f类对象都有一个返回值为void、参数为空且方法体非空finalize()方法，
    // 在f类对象创建的时候，jvm同时也会创建一个Finalizer对象引用这个f类对象， Finalizer对象创建时就被加入到了unfialized里面;

    private static Finalizer unfinalized = null;

    // 操作unfinalized队列的全局锁，入队出队操作都需要加锁
    private static final Object lock = new Object();

    // 队列链表中后一个对象和前一个对象的引用，由此可见它是一个双向链表
    private Finalizer next = null, prev = null;

    private boolean hasBeenFinalized() {
        return (next == this);
    }

    //调用add方法，将对象入队
    private void add() {
        synchronized (lock) {
            if (unfinalized != null) {
                this.next = unfinalized;
                unfinalized.prev = this;
            }
            unfinalized = this;
        }
    }

    private void remove() {
        synchronized (lock) {
            if (unfinalized == this) {
                if (this.next != null) {
                    unfinalized = this.next;
                } else {
                    unfinalized = this.prev;
                }
            }
            if (this.next != null) {
                this.next.prev = this.prev;
            }
            if (this.prev != null) {
                this.prev.next = this.next;
            }
            this.next = this;   /* Indicates that this has been finalized */
            this.prev = this;
        }
    }


    // Finalizer的构造方法是不对外暴漏，所以我们无法自己创建Finalizer对象
    private Finalizer(Object finalizee) {

        //被封装的对象和全局的f-queue
        super(finalizee, queue);
        // 调用add方法，将对象入队
        add();
    }

    /* Invoked by VM */
    // 静态的register方法，注意它的注释“被vm调用”，所以jvm是通过调用这个方法将对象封装为Finalizer对象的；
    //那么jvm又是在何时调用register方法的呢？
    //     取决于-XX:+RegisterFinalizersAtInit这个参数，默认为true，
    //     在调用构造函数返回之前调用Finalizer.register方法，如果通过-XX:-RegisterFinalizersAtInit关闭了该参数
    //     那将在对象空间分配好之后就将这个对象注册进去。所以我们创建一个重写了finalize方法的类

    static void register(Object finalizee) {
        new Finalizer(finalizee);
    }

    private void runFinalizer(JavaLangAccess jla) {
        synchronized (this) {
            if (hasBeenFinalized()) return;
            remove();
        }
        try {
            Object finalizee = this.get();
            if (finalizee != null && !(finalizee instanceof java.lang.Enum)) {
                jla.invokeFinalize(finalizee);

                /* Clear stack slot containing this variable, to decrease
                   the chances of false retention with a conservative GC */
                finalizee = null;
            }
        } catch (Throwable x) { }
        super.clear();
    }

    /* Create a privileged secondary finalizer thread in the system thread
       group for the given Runnable, and wait for it to complete.

       This method is used by both runFinalization and runFinalizersOnExit.
       The former method invokes all pending finalizers, while the latter
       invokes all uninvoked finalizers if on-exit finalization has been
       enabled.

       These two methods could have been implemented by offloading their work
       to the regular finalizer thread and waiting for that thread to finish.
       The advantage of creating a fresh thread, however, is that it insulates
       invokers of these methods from a stalled or deadlocked finalizer thread.
     */
    private static void forkSecondaryFinalizer(final Runnable proc) {
        AccessController.doPrivileged(
            new PrivilegedAction<Void>() {
                public Void run() {
                ThreadGroup tg = Thread.currentThread().getThreadGroup();
                for (ThreadGroup tgn = tg;
                     tgn != null;
                     tg = tgn, tgn = tg.getParent());
                Thread sft = new Thread(tg, proc, "Secondary finalizer");
                sft.start();
                try {
                    sft.join();
                } catch (InterruptedException x) {
                    /* Ignore */
                }
                return null;
                }});
    }

    /* Called by Runtime.runFinalization() */
    static void runFinalization() {
        if (!VM.isBooted()) {
            return;
        }

        forkSecondaryFinalizer(new Runnable() {
            private volatile boolean running;
            public void run() {
                if (running)
                    return;
                final JavaLangAccess jla = SharedSecrets.getJavaLangAccess();
                running = true;
                for (;;) {
                    Finalizer f = (Finalizer)queue.poll();
                    if (f == null) break;
                    f.runFinalizer(jla);
                }
            }
        });
    }

    /* Invoked by java.lang.Shutdown */
    static void runAllFinalizers() {
        if (!VM.isBooted()) {
            return;
        }

        forkSecondaryFinalizer(new Runnable() {
            private volatile boolean running;
            public void run() {
                if (running)
                    return;
                final JavaLangAccess jla = SharedSecrets.getJavaLangAccess();
                running = true;
                for (;;) {
                    Finalizer f;
                    synchronized (lock) {
                        f = unfinalized;
                        if (f == null) break;
                        unfinalized = f.next;
                    }
                    f.runFinalizer(jla);
                }}});
    }

    //
    private static class FinalizerThread extends Thread {

        private volatile boolean running;

        FinalizerThread(ThreadGroup g) {
            super(g, "Finalizer");
        }
        public void run() {
            // 如果-run-方法已经在执行了，直接退出；running值会在后面标记为true
            if (running)
                return;

            // Finalizer thread starts before System.initializeSystemClass
            // is called.  Wait until JavaLangAccess is available
            // 等待jvm初始化完成后才继续执行
            while (!VM.isBooted()) {
                // delay until VM completes initialization
                try {
                    VM.awaitBooted();
                } catch (InterruptedException x) {
                    // ignore and continue
                }
            }
            final JavaLangAccess jla = SharedSecrets.getJavaLangAccess();
            running = true;
            for (;;) {
                try {
                    //将对象从ReferenceQueue中移除，
                    Finalizer f = (Finalizer)queue.remove();
                    //通过-runFinalizer-调用finalizer方法
                    f.runFinalizer(jla);
                } catch (InterruptedException x) {
                    // ignore and continue
                }
            }
        }
    }

    // Finalizer静态代码块里启动了一个deamon线程，我们通过jstack命令查看线程时，总会看到一个Finalizer线程，就是这个原因:

    // -静态代码块-初始化启动FinalizerThread线程，注意它的优先级是Thread.MAX_PRIORITY– 2 = 8，
    // 很多文章说它的优先级很低，这是不对的，java里面线程优先级默认都是5；
    static {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        for (ThreadGroup tgn = tg;
             tgn != null;
             tg = tgn, tgn = tg.getParent());
        // FinalizerThread run方法是不断的从queue中去取Finalizer类型的reference，然后执行runFinalizer释放方。
        Thread finalizer = new FinalizerThread(tg);
        finalizer.setPriority(Thread.MAX_PRIORITY - 2);
        finalizer.setDaemon(true);
        finalizer.start();
    }

}
