/*
 * Copyright (c) 1997, 2013, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package java.lang.ref;

import sun.misc.Cleaner;
import sun.misc.JavaLangRefAccess;
import sun.misc.SharedSecrets;

/**
 * Abstract base class for reference objects.  This class defines the
 * operations common to all reference objects.  Because reference objects are
 * implemented in close cooperation with the garbage collector, this class may
 * not be subclassed directly.
 *
 * @author   Mark Reinhold
 * @since    1.2
 */

public abstract class Reference<T> {

    /* A Reference instance is in one of four possible internal states:
     *     Reference对象的四种状态
     *
     *     Active:活动状态,对象存在强引用状态,还没有被回收;
     *     Active: Subject to special treatment by the garbage collector.  Some
     *     time after the collector detects that the reachability of the
     *     referent has changed to the appropriate state, it changes the
     *     instance's state to either Pending or Inactive, depending upon
     *     whether or not the instance was registered with a queue when it was
     *     created.  In the former case it also adds the instance to the
     *     pending-Reference list.  Newly-created instances are Active.
     *
     *     Pending:垃圾回收器将没有强引用的Reference对象放入到pending队列中，等待ReferenceHander线程处理
     *     （前提是这个Reference对象创建的时候传入了ReferenceQueue，否则的话对象会直接进入Inactive状态）；
     *     Pending: An element of the pending-Reference list, waiting to be
     *     enqueued by the Reference-handler thread.  Unregistered instances
     *     are never in this state.
     *
     *     Enqueued:ReferenceHander线程将pending队列中的对象取出来放到ReferenceQueue队列里；
     *     Enqueued: An element of the queue with which the instance was
     *     registered when it was created.  When an instance is removed from
     *     its ReferenceQueue, it is made Inactive.  Unregistered instances are
     *     never in this state.
     *
     *     Inactive: 处于此状态的Reference对象可以被回收,并且其内部封装的对象也可以被回收掉了，有两个路径可以进入此状态，
     *     Inactive: Nothing more to do.  Once an instance becomes Inactive its
     *     state will never change again.
     *
     *
     *
     *
     * 有两个路径可以进入此状态，
     * The state is encoded in the queue and next fields as follows:
     *
     *     路径一：在创建时没有传入ReferenceQueue的Reference对象，被Reference封装的对象在没有强引用时，指向它的Reference对象会直接进入此状态；
     *     Active: queue = ReferenceQueue with which instance is registered, or
     *     ReferenceQueue.NULL if it was not registered with a queue; next =
     *     null.
     *
     *     路径二、此Reference对象经过前面三个状态后，已经由外部从ReferenceQueue中获取到,并且已经处理掉了。
     *     Pending: queue = ReferenceQueue with which instance is registered;
     *     next = this
     *
     *     Enqueued: queue = ReferenceQueue.ENQUEUED; next = Following instance
     *     in queue, or this if at end of list.
     *
     *     Inactive: queue = ReferenceQueue.NULL; next = this.
     *
     * With this scheme the collector need only examine the next field in order
     * to determine whether a Reference instance requires special treatment: If
     * the next field is null then the instance is active; if it is non-null,
     * then the collector should treat the instance normally.
     *
     * To ensure that a concurrent collector can discover active Reference
     * objects without interfering with application threads that may apply
     * the enqueue() method to those objects, collectors should link
     * discovered objects through the discovered field. The discovered
     * field is also used for linking Reference objects in the pending list.
     */

    // 所引用的对象
    private T referent;         /* Treated specially by GC */

    /*
    *
    * ReferenceQueue队列的作用就是Reference引用的对象被回收时，Reference对象能否进入到pending队列，
    * 最终由ReferenceHander线程处理后，Reference就被放到了这个队列里面（Cleaner对象除外，后面有一篇专门讲Cleaner对象源码），
    * 然后我们就可以在这个ReferenceQueue里拿到reference,
    * 执行我们自己的操作（至于什么操作就看你想怎么用了），所以这个队列起到一个对象被回收时通知的作用；
    *
    * */
    // ReferenceQueue队列，这就是我们前面提到的起到通知作用的ReferenceQueue，
    // 需要注意的是ReferenceQueue并不是一个链表数据结构，它只持有这个链表的表头对象header，
    // 这个链表是由Refence对象里面的next成员变量构建起来的，next也就是链表当前节点的下一个节点(只有next的引用，它是单向链表)，
    // 所以Reference对象本身就是一个链表的节点，这个链表数据来源前面讲pending队列的时候已经提到了，
    // 它是由ReferenceHander线程从pending队列中取的数据构建的（需要注意的是，这个对象并不是一个全局的，
    // 它是在构造方法里面传入进来的，所以Reference对象需要进入那个队列是我们自己指定的，也有特例，
    // 如FinalReference，Cleaner就是一个内部全局唯一的，无法指定，后面有两篇专门讲他们俩的源码），
    // 一旦Reference对象放入了队列里面，那么queue就会被设置为ReferenceQueue.ENQUEUED，
    // 来标识当前Reference已经进入到队里里面了；
    volatile ReferenceQueue<? super T> queue;

    /* When active:   NULL
     *     pending:   this
     *    Enqueued:   next reference in queue (or this if last)
     *    Inactive:   this
     */
    @SuppressWarnings("rawtypes")
    //用来与queue成员变量一同组成ReferenceQueue队列，见上面queue的说明；
    Reference next;

    /* When active:   next element in a discovered reference list maintained by GC (or this if last)
     *     pending:   next element in the pending list (or null if last)
     *   otherwise:   NULL
     */
    //与成员变量pending一起组成pending队列，指向链表当前节点的下一个节点
    transient private Reference<T> discovered;  /* used by VM */


    /* Object used to synchronize with the garbage collector.  The collector
     * must acquire this lock at the beginning of each collection cycle.  It is
     * therefore critical that any code holding this lock complete as quickly
     * as possible, allocate no new objects, and avoid calling user code.
     */
    // lock成员变量是pending队列的全局锁，如果你搜索这个lock变量会发现它只在ReferenceHander线程run方法里面用到了，
    // 不要忘了jvm垃圾回收器线程也会操作pending队列，往pending里面添加Reference对象，所以需要加锁；
    static private class Lock { }
    private static Lock lock = new Lock();


    /* List of References waiting to be enqueued.  The collector adds
     * References to this list, while the Reference-handler thread removes
     * them.  This list is protected by the above lock object. The
     * list uses the discovered field to link its elements.
     */
    // 未决定的；行将发生的
    //
    //pending队列， pending成员变量与后面的discovered对象一起构成了一个pending单向链表，注意这个成员变量是一个静态对象，
    // 所以是全局唯一的，pending为链表的头节点，discovered为链表当前Reference节点指向下一个节点的引用，
    // 这个队列是由jvm的垃圾回收器构建的，当对象除了被reference引用之外没有其它强引用了，
    // jvm的垃圾回收器就会将指向需要回收的对象的Reference都放入到这个队列里面
    // （好好理解一下这句话，注意是指向要回收的对象的Reference，要回收的对象就是Reference的成员变量refernt持有的对象，
    // 是refernt持有的对象要被回收，而不是Reference对象本身），这个队列会由ReferenceHander线程来处理
    // （ReferenceHander线程是jvm的一个内部线程，它也是Reference的一个内部类，它的任务就是将pending队列中要被回收的Reference对象移除出来，
    // 如果Reference对象在初始化的时候传入了ReferenceQueue队列，那么就把从pending队列里面移除的Reference放到它自己的ReferenceQueue队列里
    // （为什么是它自己的？pending队列是全局唯一的队列，但是Reference的queue却不是，它是在构造方法里面指定的，前面说过这里Cleaner对象是个特例），
    // 如果没有ReferenceQueue队列，那么其关联的对象就不会进入到Pending队列中，会直接被回收掉，除此之外ReferenceHander线程还会做一些其它操作，
    // 后面会讲到
    private static Reference<Object> pending = null;

    /* High-priority thread to enqueue pending References
     */




    /*
    *
    * ReferenceHandler线程是一个拥有最高优先级的守护线程，
    * 它是Reference类的一个内部类，在Reference类加载执行cinit的时候被初始化并启动；
    * 它的任务就是当pending队列不为空的时候，循环将pending队列里面的头部的Reference移除出来，
    * 如果这个对象是个Cleaner实例，那么就直接执行它的clean方法来执行清理工作；
    * 否则放入到它自己的ReferenceQueue里面；
    * 所以这个线程是pending队列与ReferenceQueue的桥梁；
    * */

    private static class ReferenceHandler extends Thread {

        // 确保类被引入；
        private static void ensureClassInitialized(Class<?> clazz) {
            try {
                Class.forName(clazz.getName(), true, clazz.getClassLoader());
            } catch (ClassNotFoundException e) {
                throw (Error) new NoClassDefFoundError(e.getMessage()).initCause(e);
            }
        }

        static {
            // pre-load and initialize InterruptedException and Cleaner classes
            // so that we don't get into trouble later in the run loop if there's
            // memory shortage while loading/initializing them lazily.
            ensureClassInitialized(InterruptedException.class);
            ensureClassInitialized(Cleaner.class);
        }

        ReferenceHandler(ThreadGroup g, String name) {
            super(g, name);
        }

        public void run() {
            // 不间断的处理pending队列；
            while (true) {
                tryHandlePending(true);
            }
        }
    }

    /**
     * Try handle pending {@link Reference} if there is one.<p>
     * Return {@code true} as a hint that there might be another
     * {@link Reference} pending or {@code false} when there are no more pending
     * {@link Reference}s at the moment and the program can do some other
     * useful work instead of looping.
     *
     * @param waitForNotify if {@code true} and there was no pending
     *                      {@link Reference}, wait until notified from VM
     *                      or interrupted; if {@code false}, return immediately
     *                      when there is no pending {@link Reference}.
     * @return {@code true} if there was a {@link Reference} pending and it
     *         was processed, or we waited for notification and either got it
     *         or thread was interrupted before being notified;
     *         {@code false} otherwise.
     */
    static boolean tryHandlePending(boolean waitForNotify) {
        //从pending中移除的Reference对象
        Reference<Object> r;
        Cleaner c;
        try {


            //此处需要加全局锁，因为除了当前线程，gc线程也会操作pending队列
            synchronized (lock) {
                //如果pending队列不为空，则将第一个Reference对象取出
                if (pending != null) {
                    //缓存pending队列头节点
                    r = pending;
                    // 'instanceof' might throw OutOfMemoryError sometimes
                    // so do this before un-linking 'r' from the 'pending' chain...
                    c = r instanceof Cleaner ? (Cleaner) r : null;
                    // unlink 'r' from 'pending' chain
                    //将头节点指向discovered，discovered为pending队列中当前节点的下一个节点，这样就把第一个头结点出队了
                    pending = r.discovered;
                    //将头节点指向discovered，discovered为pending队列中当前节点的下一个节点，这样就把第一个头结点出队了
                    r.discovered = null;
                } else {
                    // The waiting on the lock may cause an OutOfMemoryError
                    // because it may try to allocate exception objects.
                    //如果pending队列为空，则等待
                    if (waitForNotify) {
                        lock.wait();
                    }
                    // retry if waited
                    return waitForNotify;
                }
            }
        } catch (OutOfMemoryError x) {
            // Give other threads CPU time so they hopefully drop some live references
            // and GC reclaims some space.
            // Also prevent CPU intensive spinning in case 'r instanceof Cleaner' above
            // persistently throws OOME for some time...
            Thread.yield();
            // retry
            return true;
        } catch (InterruptedException x) {
            // retry
            return true;
        }

        // Fast path for cleaners
        // 如果从pending队列出队的r是一个Cleaner对象，那么直接执行其clean()方法执行清理操作；
        if (c != null) {
            c.clean();
            // 注意这里，这里已经不往下执行了，所以Cleaner对象是不会进入到队列里面的，
            // 给它设置ReferenceQueue的作用是为了让它能进入Pending队列后被ReferenceHander线程处理；
            return true;
        }

        //将对象放入到它自己的ReferenceQueue队列里
        ReferenceQueue<? super Object> q = r.queue;
        if (q != ReferenceQueue.NULL) q.enqueue(r);
        return true;
    }

    //  以下是ReferenceHander线程初始化并启动的操作
    static {
        ThreadGroup tg = Thread.currentThread().getThreadGroup();
        // 找到最？
        for (ThreadGroup tgn = tg; tgn != null; tg = tgn, tgn = tg.getParent());

        //线程名称为Reference Handler
        Thread handler = new ReferenceHandler(tg, "Reference Handler");
        /* If there were a special system-only priority greater than
         * MAX_PRIORITY, it would be used here
         */
        // 设置为最大权限的守护线程；
        handler.setPriority(Thread.MAX_PRIORITY);
        handler.setDaemon(true);
        handler.start();

        // provide access in SharedSecrets
        SharedSecrets.setJavaLangRefAccess(new JavaLangRefAccess() {
            @Override
            public boolean tryHandlePendingReference() {
                return tryHandlePending(false);
            }
        });
    }

    /* -- Referent accessor and setters -- */

    /**
     * Returns this reference object's referent.  If this reference object has
     * been cleared, either by the program or by the garbage collector, then
     * this method returns <code>null</code>.
     *
     * @return   The object to which this reference refers, or
     *           <code>null</code> if this reference object has been cleared
     */
    public T get() {
        return this.referent;
    }

    /**
     * Clears this reference object.  Invoking this method will not cause this
     * object to be enqueued.
     *
     * <p> This method is invoked only by Java code; when the garbage collector
     * clears references it does so directly, without invoking this method.
     */
    public void clear() {
        this.referent = null;
    }


    /* -- Queue operations -- */

    /**
     * Tells whether or not this reference object has been enqueued, either by
     * the program or by the garbage collector.  If this reference object was
     * not registered with a queue when it was created, then this method will
     * always return <code>false</code>.
     *
     * @return   <code>true</code> if and only if this reference object has
     *           been enqueued
     */
    public boolean isEnqueued() {
        return (this.queue == ReferenceQueue.ENQUEUED);
    }

    /**
     * Adds this reference object to the queue with which it is registered,
     * if any.
     *
     * <p> This method is invoked only by Java code; when the garbage collector
     * enqueues references it does so directly, without invoking this method.
     *
     * @return   <code>true</code> if this reference object was successfully
     *           enqueued; <code>false</code> if it was already enqueued or if
     *           it was not registered with a queue when it was created
     */
    public boolean enqueue() {
        return this.queue.enqueue(this);
    }


    /* -- Constructors -- */
    //
    Reference(T referent) {
        this(referent, null);
    }

    Reference(T referent, ReferenceQueue<? super T> queue) {
        this.referent = referent;
        this.queue = (queue == null) ? ReferenceQueue.NULL : queue;
    }

}
