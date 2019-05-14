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

/**
 * Reference queues, to which registered reference objects are appended by the
 * garbage collector after the appropriate reachability changes are detected.
 *
 * @author   Mark Reinhold
 * @since    1.2
 */
// ReferenceQueue 提供队列的功能，有入队(enqueue)和出队(poll,remove,其中remove阻塞等待提取队列元素);

// ReferenceQueue队列是一个单向链表，ReferenceQueue里面只有一个header成员变量持有队列的队头，
// Reference对象是从队头做出队入队操作，所以它是一个后进先出的队列




public class ReferenceQueue<T> {

    /**
     * Constructs a new reference-object queue.
     */
    public ReferenceQueue() { }

    // 私有静态内部类；
    // 内部类，它是用来做状态识别的，重写了enqueue入队方法，
    // 永远返回false，所以它不会存储任何数据，见后面的NULL和ENQUEUED两个标识成员变量;
    private static class Null<S> extends ReferenceQueue<S> {
        boolean enqueue(Reference<? extends S> r) {
            return false;
        }
    }

    // ReferenceQueue提供了两个静态字段NULL，ENQUEUED
    // 这两个字段的主要功能：NULL是当我们构造Reference实例时queue传入null时，
    // 会默认使用NULL，这样在enqueue时判断queue是否为NULL,如果为NULL直接返回，
    // 入队失败。ENQUEUED的作用是防止重复入队，reference后会把其queue字段赋值为ENQUEUED,当再次入队时会直接返回失败。
    //当Reference对象创建时没有指定queue或Reference对象已经处于inactive状态
    static ReferenceQueue<Object> NULL = new Null<>();
    // ENQUEUED的作用是防止重复入队，reference后会把其queue字段赋值为ENQUEUED,当再次入队时会直接返回失败。
    // 当Reference已经被ReferenceHander线程从pending队列移到queue里面时
    static ReferenceQueue<Object> ENQUEUED = new Null<>();

    // 锁
    // //出队入队时对队列加锁
    static private class Lock { };
    private Lock lock = new Lock();


    // ReferenceQueue对象本身保存了一个Reference类型的head节点；
    // Reference封装了next字段，这样就是可以组成一个单向链表。
    //队列头
    private volatile Reference<? extends T> head = null;
    //队列长度
    private long queueLength = 0;

    // 有入队(enqueue)和出队(poll,remove,其中remove阻塞等待提取队列元素)
    // 入队操作，ReferenceHander调用此方法将Reference放入到队列里
    boolean enqueue(Reference<? extends T> r) { /* Called only by Reference class */

        // 加锁操作队列
        synchronized (lock) {
            // Check that since getting the lock this reference hasn't already been
            // enqueued (and even then removed)

            ReferenceQueue<?> queue = r.queue;

            // 如果Reference创建时没有指定队列或Reference对象已经在队列里面了，则直接返回

            if ((queue == NULL) || (queue == ENQUEUED)) {
                return false;
            }
            //只有r的队列是当前队列才允许入队
            assert queue == this;
            //将 r 的queue设置为ENQUEUED状态，标识Reference已经入队
            r.queue = ENQUEUED;
            //从队列头部入队
            r.next = (head == null) ? r : head;
            head = r;
            //队列里面对象数量+1
            queueLength++;
            //如果r是一个FinalReference实例，那么将FinalReference数量也+1
            if (r instanceof FinalReference) {
                sun.misc.VM.addFinalRefCount(1);
            }
            //唤醒出队操作的等待线程
            lock.notifyAll();
            return true;
        }
    }

    @SuppressWarnings("unchecked")

    // Reference-对象出队操作，将头部第一个对象移出队列，并将队列长度-1
    private Reference<? extends T> reallyPoll() {       /* Must hold lock */
        Reference<? extends T> r = head;
        if (r != null) {
            head = (r.next == r) ?
                null :
                r.next; // Unchecked due to the next field having a raw type in Reference
            r.queue = NULL;
            r.next = r;
            queueLength--;
            if (r instanceof FinalReference) {
                sun.misc.VM.addFinalRefCount(-1);
            }
            return r;
        }
        return null;
    }

    /**
     * Polls this queue to see if a reference object is available.  If one is
     * available without further delay then it is removed from the queue and
     * returned.  Otherwise this method immediately returns <tt>null</tt>.
     *
     * @return  A reference object, if one was immediately available,
     *          otherwise <code>null</code>
     */
    // 将-队列头部第一个对象-从队列中移除出来，如果队列为空则直接返回null（此方法不会被阻塞）
    public Reference<? extends T> poll() {
        if (head == null)
            return null;
        synchronized (lock) {
            return reallyPoll();
        }
    }

    /**
     * Removes the next reference object in this queue, blocking until either
     * one becomes available or the given timeout period expires.
     *
     * <p> This method does not offer real-time guarantees: It schedules the
     * timeout as if by invoking the {@link Object#wait(long)} method.
     *
     * @param  timeout  If positive, block for up to <code>timeout</code>
     *                  milliseconds while waiting for a reference to be
     *                  added to this queue.  If zero, block indefinitely.
     *
     * @return  A reference object, if one was available within the specified
     *          timeout period, otherwise <code>null</code>
     *
     * @throws  IllegalArgumentException
     *          If the value of the timeout argument is negative
     *
     * @throws  InterruptedException
     *          If the timeout wait is interrupted
     */
    // 将头部- 第一个对象移出队列并返回，如果队列为空，则等待timeout时间后，返回null，这个方法会阻塞线程;
    public Reference<? extends T> remove(long timeout)
        throws IllegalArgumentException, InterruptedException
    {
        if (timeout < 0) {
            throw new IllegalArgumentException("Negative timeout value");
        }
        synchronized (lock) {
            Reference<? extends T> r = reallyPoll();
            if (r != null) return r;
            //
            long start = (timeout == 0) ? 0 : System.nanoTime();
            for (;;) {
                lock.wait(timeout);
                r = reallyPoll();
                if (r != null) return r;
                if (timeout != 0) {
                    long end = System.nanoTime();
                    timeout -= (end - start) / 1000_000;
                    if (timeout <= 0) return null;
                    start = end;
                }
            }
        }
    }

    /**
     * Removes the next reference object in this queue, blocking until one
     * becomes available.
     *
     * @return A reference object, blocking until one becomes available
     * @throws  InterruptedException  If the wait is interrupted
     */
    public Reference<? extends T> remove() throws InterruptedException {
        return remove(0);
    }

}
