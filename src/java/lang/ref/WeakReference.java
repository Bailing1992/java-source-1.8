/*
 * Copyright (c) 1997, 2003, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *     弱引用对象的存在不会阻止它所指向的对象被垃圾回收器回收。
 * 弱引用最常见的用途是实现规范映射(canonicalizing mappings，比如哈希表）。
 *     假设垃圾收集器在某个时间点决定一个对象是弱可达的(weakly reachable)（也就是说当前指向它的全都是弱引用），
 * 这时垃圾收集器会清除所有指向该对象的弱引用，然后垃圾收集器会把这个弱可达对象标记为可终结(finalizable)的，
 * 这样它们随后就会被回收。与此同时或稍后，
 * 垃圾收集器会把那些刚清除的弱引用放入创建弱引用对象时所登记到的引用队列(Reference Queue)中。
 *
 *
 */

package java.lang.ref;


/**
 * Weak reference objects, which do not prevent their referents from being
 * made finalizable, finalized, and then reclaimed.  Weak references are most
 * often used to implement canonicalizing mappings.
 *
 * <p> Suppose that the garbage collector determines at a certain point in time
 * that an object is <a href="package-summary.html#reachability">weakly
 * reachable</a>.  At that time it will atomically clear all weak references to
 * that object and all weak references to any other weakly-reachable objects
 * from which that object is reachable through a chain of strong and soft
 * references.  At the same time it will declare all of the formerly
 * weakly-reachable objects to be finalizable.  At the same time or at some
 * later time it will enqueue those newly-cleared weak references that are
 * registered with reference queues.
 *
 * @author   Mark Reinhold
 * @since    1.2
 */

public class WeakReference<T> extends Reference<T> {

    /**
     * Creates a new weak reference that refers to the given object.  The new
     * reference is not registered with any queue.
     *
     * @param referent object the new weak reference will refer to
     */
    public WeakReference(T referent) {
        super(referent);
    }

    /**
     * Creates a new weak reference that refers to the given object and is
     * registered with the given queue.
     *
     * @param referent object the new weak reference will refer to
     * @param q the queue with which the reference is to be registered,
     *          or <tt>null</tt> if registration is not required
     */
    public WeakReference(T referent, ReferenceQueue<? super T> q) {
        super(referent, q);
    }

}
