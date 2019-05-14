package javaTest.lang.ref.PhantomReference;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.List;


/*
*
* PhantomReference主要作为其指向的referent被回收时的一种通知机制,
* 它就是利用上文讲到的ReferenceQueue实现的。当referent被gc回收时，
* JVM自动把PhantomReference对象(reference)本身加入到ReferenceQueue中，
* 像发出信号通知一样，表明该reference指向的referent被回收。
* 然后可以通过去queue中取到reference，此时说明其指向的referent已经被回收，
* 可以通过这个通知机制来做额外的清场工作。 因此有些情况可以用PhantomReference
* 代替finalize()，做资源释放更明智。
*
* */
public class ResourcePhantomReference<T> extends PhantomReference<T> {

    private List<Closeable> closeables;

    //
    public ResourcePhantomReference(T referent, ReferenceQueue<? super T> q, List<Closeable> resource) {
        super(referent, q);
        closeables = resource;
    }

    public void cleanUp() {
        if (closeables == null || closeables.size() == 0)
            return;
        for (Closeable closeable : closeables) {
            try {
                closeable.close();
                System.out.println("clean up:"+closeable);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
