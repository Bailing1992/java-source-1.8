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

package java.util;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Hash table based implementation of the <tt>Map</tt> interface.  This
 * implementation provides all of the optional map operations, and permits
 * <tt>null</tt> values and the <tt>null</tt> key.  (The <tt>HashMap</tt>
 * class is roughly equivalent to <tt>Hashtable</tt>, except that it is
 * unsynchronized and permits nulls.)  This class makes no guarantees as to
 * the order of the map; in particular, it does not guarantee that the order
 * will remain constant over time.
 *
 * <p>This implementation provides constant-time performance for the basic
 * operations (<tt>get</tt> and <tt>put</tt>), assuming the hash function
 * disperses the elements properly among the buckets.  Iteration over
 * collection views requires time proportional to the "capacity" of the
 * <tt>HashMap</tt> instance (the number of buckets) plus its size (the number
 * of key-value mappings).  Thus, it's very important not to set the initial
 * capacity too high (or the load factor too low) if iteration performance is
 * important.
 *
 * <p>An instance of <tt>HashMap</tt> has two parameters that affect its
 * performance: <i>initial capacity</i> and <i>load factor</i>.  The
 * <i>capacity</i> is the number of buckets in the hash table, and the initial
 * capacity is simply the capacity at the time the hash table is created.  The
 * <i>load factor</i> is a measure of how full the hash table is allowed to
 * get before its capacity is automatically increased.  When the number of
 * entries in the hash table exceeds the product of the load factor and the
 * current capacity, the hash table is <i>rehashed</i> (that is, internal data
 * structures are rebuilt) so that the hash table has approximately twice the
 * number of buckets.
 *
 * <p>As a general rule, the default load factor (.75) offers a good
 * tradeoff between time and space costs.  Higher values decrease the
 * space overhead but increase the lookup cost (reflected in most of
 * the operations of the <tt>HashMap</tt> class, including
 * <tt>get</tt> and <tt>put</tt>).  The expected number of entries in
 * the map and its load factor should be taken into account when
 * setting its initial capacity, so as to minimize the number of
 * rehash operations.  If the initial capacity is greater than the
 * maximum number of entries divided by the load factor, no rehash
 * operations will ever occur.
 *
 * <p>If many mappings are to be stored in a <tt>HashMap</tt>
 * instance, creating it with a sufficiently large capacity will allow
 * the mappings to be stored more efficiently than letting it perform
 * automatic rehashing as needed to grow the table.  Note that using
 * many keys with the same {@code hashCode()} is a sure way to slow
 * down performance of any hash table. To ameliorate impact, when keys
 * are {@link Comparable}, this class may use comparison order among
 * keys to help break ties.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access a hash map concurrently, and at least one of
 * the threads modifies the map structurally, it <i>must</i> be
 * synchronized externally.  (A structural modification is any operation
 * that adds or deletes one or more mappings; merely changing the value
 * associated with a key that an instance already contains is not a
 * structural modification.)  This is typically accomplished by
 * synchronizing on some object that naturally encapsulates the map.
 *
 * If no such object exists, the map should be "wrapped" using the
 * {@link Collections#synchronizedMap Collections.synchronizedMap}
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the map:<pre>
 *   Map m = Collections.synchronizedMap(new HashMap(...));</pre>
 *
 * <p>The iterators returned by all of this class's "collection view methods"
 * are <i>fail-fast</i>: if the map is structurally modified at any time after
 * the iterator is created, in any way except through the iterator's own
 * <tt>remove</tt> method, the iterator will throw a
 * {@link ConcurrentModificationException}.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the
 * future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw <tt>ConcurrentModificationException</tt> on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness: <i>the fail-fast behavior of iterators
 * should be used only to detect bugs.</i>
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/../technotes/guides/collections/index.html">
 * Java Collections Framework</a>.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @author  Doug Lea
 * @author  Josh Bloch
 * @author  Arthur van Hoff
 * @author  Neal Gafter
 * @see     Object#hashCode()
 * @see     Collection
 * @see     Map
 * @see     TreeMap
 * @see     Hashtable
 * @since   1.2
 */
public class HashMap<K,V> extends AbstractMap<K,V> implements Map<K,V>, Cloneable, Serializable {

    private static final long serialVersionUID = 362498820763181265L;

    /*
     * Implementation notes.
     *
     * This map usually acts as a binned (bucketed) hash table, but
     * when bins get too large, they are transformed into bins of
     * TreeNodes, each structured similarly to those in
     * java.util.TreeMap. Most methods try to use normal bins, but
     * relay to TreeNode methods when applicable (simply by checking
     * instanceof a node).  Bins of TreeNodes may be traversed and
     * used like any others, but additionally support faster lookup
     * when overpopulated. However, since the vast majority of bins in
     * normal use are not overpopulated, checking for existence of
     * tree bins may be delayed in the course of table methods.
     *
     * Tree bins (i.e., bins whose elements are all TreeNodes) are
     * ordered primarily by hashCode, but in the case of ties, if two
     * elements are of the same "class C implements Comparable<C>",
     * type then their compareTo method is used for ordering. (We
     * conservatively check generic types via reflection to validate
     * this -- see method comparableClassFor).  The added complexity
     * of tree bins is worthwhile in providing worst-case O(log n)
     * operations when keys either have distinct hashes or are
     * orderable, Thus, performance degrades gracefully under
     * accidental or malicious usages in which hashCode() methods
     * return values that are poorly distributed, as well as those in
     * which many keys share a hashCode, so long as they are also
     * Comparable. (If neither of these apply, we may waste about a
     * factor of two in time and space compared to taking no
     * precautions. But the only known cases stem from poor user
     * programming practices that are already so slow that this makes
     * little difference.)
     *
     * Because TreeNodes are about twice the size of regular nodes, we
     * use them only when bins contain enough nodes to warrant use
     * (see TREEIFY_THRESHOLD). And when they become too small (due to
     * removal or resizing) they are converted back to plain bins.  In
     * usages with well-distributed user hashCodes, tree bins are
     * rarely used.  Ideally, under random hashCodes, the frequency of
     * nodes in bins follows a Poisson distribution
     * (http://en.wikipedia.org/wiki/Poisson_distribution) with a
     * parameter of about 0.5 on average for the default resizing
     * threshold of 0.75, although with a large variance because of
     * resizing granularity. Ignoring variance, the expected
     * occurrences of list size k are (exp(-0.5) * pow(0.5, k) /
     * factorial(k)). The first values are:
     *
     * 0:    0.60653066
     * 1:    0.30326533
     * 2:    0.07581633
     * 3:    0.01263606
     * 4:    0.00157952
     * 5:    0.00015795
     * 6:    0.00001316
     * 7:    0.00000094
     * 8:    0.00000006
     * more: less than 1 in ten million
     *
     * The root of a tree bin is normally its first node.  However,
     * sometimes (currently only upon Iterator.remove), the root might
     * be elsewhere, but can be recovered following parent links
     * (method TreeNode.root()).
     *
     * All applicable internal methods accept a hash code as an
     * argument (as normally supplied from a public method), allowing
     * them to call each other without recomputing user hashCodes.
     * Most internal methods also accept a "tab" argument, that is
     * normally the current table, but may be a new or old one when
     * resizing or converting.
     *
     * When bin lists are treeified, split, or untreeified, we keep
     * them in the same relative access/traversal order (i.e., field
     * Node.next) to better preserve locality, and to slightly
     * simplify handling of splits and traversals that invoke
     * iterator.remove. When using comparators on insertion, to keep a
     * total ordering (or as close as is required here) across
     * rebalancings, we compare classes and identityHashCodes as
     * tie-breakers.
     *
     * The use and transitions among plain vs tree modes is
     * complicated by the existence of subclass LinkedHashMap. See
     * below for hook methods defined to be invoked upon insertion,
     * removal and access that allow LinkedHashMap internals to
     * otherwise remain independent of these mechanics. (This also
     * requires that a map instance be passed to some utility methods
     * that may create new nodes.)
     *
     * The concurrent-programming-like SSA-based coding style helps
     * avoid aliasing errors amid all of the twisty pointer operations.
     */

    /**
     * The default initial capacity - MUST be a power of two.
     */
    // 默认的初始容量是16，必须是2的幂。 default initial capacity
    static final int DEFAULT_INITIAL_CAPACITY = 1 << 4; // aka 16

    /**
     * The maximum capacity, used if a higher value is implicitly specified
     * by either of the constructors with arguments.
     * MUST be a power of two <= 1<<30.
     */
    // 最大容量（必须是2的幂且小于2的30次方，传入容量过大将被这个值替换）
            // 0100000...得确已是最大
    static final int MAXIMUM_CAPACITY = 1 << 30;

    /**
     * The load factor used when none specified in constructor.
     */
    // 通常，默认加载因子是 0.75;
    //
    static final float DEFAULT_LOAD_FACTOR = 0.75f;

    /**
     * The bin count threshold for using a tree rather than list for a
     * bin.  Bins are converted to trees when adding an element to a
     * bin with at least this many nodes. The value must be greater
     * than 2 and should be at least 8 to mesh with assumptions in
     * tree removal about conversion back to plain bins upon
     * shrinkage.
     */
    // treeify threshold
    //  链表节点转换红黑树节点的阈值, 8个节点就转
    static final int TREEIFY_THRESHOLD = 8;

    /**
     * The bin count threshold for untreeifying a (split) bin during a
     * resize operation. Should be less than TREEIFY_THRESHOLD, and at
     * most 6 to mesh with shrinkage detection under removal.
     */

    // 红黑树节点转换链表节点的阈值, 6个节点转
    static final int UNTREEIFY_THRESHOLD = 6;

    /**
     * The smallest table capacity for which bins may be treeified.
     * (Otherwise the table is resized if too many nodes in a bin.)
     * Should be at least 4 * TREEIFY_THRESHOLD to avoid conflicts
     * between resizing and treeification thresholds.
     */
    //转红黑树时, table的最小长度
    static final int MIN_TREEIFY_CAPACITY = 64;

    /**
     * Basic hash bin node, used for most entries.  (See below for
     * TreeNode subclass, and in LinkedHashMap for its Entry subclass.)
     */

    // 基本的哈希存储节点，
    static class Node<K,V> implements Map.Entry<K,V> {
        // 存储Node的哈希值；
        final int hash;
        //
        final K key;
        // 值可变
        V value;

        Node<K,V> next;

        Node(int hash, K key, V value, Node<K,V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }

        public final K getKey()        { return key; }

        public final V getValue()      { return value; }

        public final String toString() { return key + "=" + value; }

        // Objects.hashCode(key)没有则返回零；
        public final int hashCode() {
            return Objects.hashCode(key) ^ Objects.hashCode(value);
        }

        public final V setValue(V newValue) {
            V oldValue = value;
            value = newValue;
            return oldValue;
        }

        public final boolean equals(Object o) {
            if (o == this)
                return true;
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>)o;
                if (Objects.equals(key, e.getKey()) &&
                    Objects.equals(value, e.getValue()))
                    //
                    return true;
            }
            return false;
        }
    }

    /* ---------------- Static utilities -------------- */

    /**
     * Computes key.hashCode() and spreads (XORs) higher bits of hash
     * to lower.  Because the table uses power-of-two masking, sets of
     * hashes that vary only in bits above the current mask will
     * always collide. (Among known examples are sets of Float keys
     * holding consecutive whole numbers in small tables.)  So we
     * apply a transform that spreads the impact of higher bits
     * downward. There is a tradeoff between speed, utility, and
     * quality of bit-spreading. Because many common sets of hashes
     * are already reasonably distributed (so don't benefit from
     * spreading), and because we use trees to handle large sets of
     * collisions in bins, we just XOR some shifted bits in the
     * cheapest possible way to reduce systematic lossage, as well as
     * to incorporate impact of the highest bits that would otherwise
     * never be used in index calculations because of table bounds.
     */
    // 在HashMap中，为了更好的性能，我们希望作为Key的对象提供一个合理的hash函数以便能将其合理的分配到桶中。

    // 计算哈希值 = key.hashCode() ^ key.hashCode()>>>16
    //ash的目的是为了希望能够尽量均匀,最后做indexFor的时候实际上只是利用了低16位,高16位是用不到的,
    // 那么低16位的数字如何保证均匀?即使利用^亦或的方法,因为&和|都会使得结果偏向0或者1 ,并不是均匀的概念
    static final int hash(Object key) {
        int h;
        return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
    }

    /**
     * Returns x's Class if it is of the form "class C implements
     * Comparable<C>", else null.
     */
    // 判断x是否实现了Comparable接口，实现了则返回x的class；
    static Class<?> comparableClassFor(Object x) {
        if (x instanceof Comparable) {

            Class<?> c;
            Type[] ts, as;
            Type t;
            ParameterizedType p;
            //x.getClass()返回类
            if ((c = x.getClass()) == String.class) // bypass checks
                return c;
            // 返回继承的接口
            if ((ts = c.getGenericInterfaces()) != null) {
                for (int i = 0; i < ts.length; ++i) {
                    if (((t = ts[i]) instanceof ParameterizedType) &&
                        ((p = (ParameterizedType)t).getRawType() ==
                         Comparable.class) &&
                        (as = p.getActualTypeArguments()) != null &&
                        as.length == 1 && as[0] == c) // type arg is c
                        return c;
                }
            }
        }
        return null;
    }

    /**
     * Returns k.compareTo(x) if x matches kc (k's screened comparable
     * class), else 0.
     */
    @SuppressWarnings({"rawtypes","unchecked"}) // for cast to Comparable
    static int compareComparables(Class<?> kc, Object k, Object x) {
        return (x == null || x.getClass() != kc ? 0 :
                ((Comparable)k).compareTo(x));
    }

    /**
     * Returns a power of two size for the given target capacity.
     *
     */
    //   该方法用来返回大于等于给定整数cap的最小2^次幂值；

    static final int tableSizeFor(int cap) {

        int n = cap - 1; //避免n=2^m这种情况，经过下面运算后导致结果比n本身大一倍
        n |= n >>> 1; //确保第一次出现1的位及其后一位都是1；
        n |= n >>> 2; //确保第一次出现1的位及其后三位都是1；
        n |= n >>> 4; //确保第一次出现1的位及其后7位都是1；
        n |= n >>> 8; //确保第一次出现1的位及其后15位都是1;
        n |= n >>> 16; //确保第一次出现1的位及其后面所有位都是1；

        return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1; //n+1即为0x0100000......00就是大于等于n的最小2^次幂
    }

    /* ---------------- Fields -------------- */

    /**
     * The table, initialized on first use, and resized as
     * necessary. When allocated, length is always a power of two.
     * (We also tolerate length zero in some operations to allow
     * bootstrapping mechanics that are currently not needed.)
     */

    // 在第一次使用时进行初始化
    transient Node<K,V>[] table;

    /**
     * Holds cached entrySet(). Note that AbstractMap fields are used
     * for keySet() and values().
     */

    transient Set<Map.Entry<K,V>> entrySet;

    /**
     * The number of key-value mappings contained in this map.
     */
    // 存储的键值对的个数；
    transient int size;

    /**
     * The number of times this HashMap has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the HashMap or otherwise modify its internal structure (e.g.,
     * rehash).  This field is used to make iterators on Collection-views of
     * the HashMap fail-fast.  (See ConcurrentModificationException).
     */
    transient int modCount;

    /**
     * The next size value at which to resize (capacity * load factor).
     *
     * @serial
     */
    // (The javadoc description is true upon serialization.
    // Additionally, if the table array has not been allocated, this
    // field holds the initial array capacity, or zero signifying
    // DEFAULT_INITIAL_CAPACITY.)
    // 对table桶进行扩容的阈值；
    int threshold;

    /**
     * The load factor for the hash table.
     *
     * @serial
     */
    final float loadFactor;

    /* ---------------- Public operations -------------- */

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and load factor.
     *
     * @param  initialCapacity the initial capacity
     * @param  loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
    public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;

        //
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);


        this.loadFactor = loadFactor;
        // HashMap进行桶扩容的阈值，它的值等于 HashMap 的容量乘以负载因子
        this.threshold = tableSizeFor(initialCapacity);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the specified initial
     * capacity and the default load factor (0.75).
     *
     * @param  initialCapacity the initial capacity.
     * @throws IllegalArgumentException if the initial capacity is negative.
     */
    public HashMap(int initialCapacity) {
        this(initialCapacity, DEFAULT_LOAD_FACTOR);
    }

    /**
     * Constructs an empty <tt>HashMap</tt> with the default initial capacity
     * (16) and the default load factor (0.75).
     */
    //该构造函数意在构造一个具有> 默认初始容量 (16) 和 默认负载因子(0.75) 的空 HashMap，
    // 是 Java Collection Framework 规范推荐提供的，其源码如下：
    public HashMap() {
        ////负载因子:用于衡量的是一个散列表的空间的使用程度
        this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
    }

    /**
     * Constructs a new <tt>HashMap</tt> with the same mappings as the
     * specified <tt>Map</tt>.  The <tt>HashMap</tt> is created with
     * default load factor (0.75) and an initial capacity sufficient to
     * hold the mappings in the specified <tt>Map</tt>.
     *
     * @param   m the map whose mappings are to be placed in this map
     * @throws  NullPointerException if the specified map is null
     */
    //
    public HashMap(Map<? extends K, ? extends V> m) {
        this.loadFactor = DEFAULT_LOAD_FACTOR;
        putMapEntries(m, false);
    }

    /**
     * Implements Map.putAll and Map constructor
     *
     * @param m the map
     * @param evict false when initially constructing this map, else
     * true (relayed to method afterNodeInsertion).
     */
    final void putMapEntries(Map<? extends K, ? extends V> m, boolean evict) {
        int s = m.size();
        if (s > 0) {
            if (table == null) { // pre-size
                float ft = ((float)s / loadFactor) + 1.0F;
                int t = ((ft < (float)MAXIMUM_CAPACITY) ?
                         (int)ft : MAXIMUM_CAPACITY);
                if (t > threshold)
                    threshold = tableSizeFor(t);
            }
            else if (s > threshold)
                resize();
            for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
                K key = e.getKey();
                V value = e.getValue();
                putVal(hash(key), key, value, false, evict);
            }
        }
    }

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return size;
    }

    /**
     * Returns <tt>true</tt> if this map contains no key-value mappings.
     *
     * @return <tt>true</tt> if this map contains no key-value mappings
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code (key==null ? k==null :
     * key.equals(k))}, then this method returns {@code v}; otherwise
     * it returns {@code null}.  (There can be at most one such mapping.)
     *
     * <p>A return value of {@code null} does not <i>necessarily</i>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}.
     * The {@link #containsKey containsKey} operation may be used to
     * distinguish these two cases.
     *
     * @see #put(Object, Object)
     */

    //
    public V get(Object key) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? null : e.value;
    }

    /**
     * Implements Map.get and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @return the node, or null if none
     */
    //
    final Node<K,V> getNode(int hash, Object key) {
        // 对对象中的属性进行备份，不直接操作；
        Node<K,V>[] tab;
        Node<K,V> first, e;
        int n;
        K k;
        // table不为空 && table长度大于0 && table索引位置(根据hash值计算出)不为空
        // (n - 1) & hash == hash mod n;
        if ((tab = table) != null &&
                (n = tab.length) > 0 &&
                //使用table.length - 1和hash值进行位与运算，得出在table上的索引位置，
                // 将该索引位置的节点赋值给first节点，校验该索引位置是否为空
                (first = tab[(n - 1) & hash]) != null) {

            // 哈希值相同，且key值相同(key指向同一个对象或相同)；
            // 检查first节点的hash值和key是否和入参的一样，如果一样则first即为目标节点，直接返回first节点
            if (first.hash == hash && ((k = first.key) == key || (key != null && key.equals(k))))
                return first;
            // 如果first的next节点不为空则继续遍历
            // first的key等于传入的key则返回first对象
            if ((e = first.next) != null) {
                if (first instanceof TreeNode)
                    // 如果first节点为TreeNode，则调用getTreeNode方法（见下文代码块1）查找目标节点
                    // 如果是红黑树节点，则调用红黑树的查找目标节点方法getTreeNode
                    return ((TreeNode<K,V>)first).getTreeNode(hash, key);
                // 走到这代表节点为链表节点；
                // 如果first节点不为TreeNode，则调用普通的遍历链表方法查找目标节点
                do {
                    // 向下遍历链表, 直至找到节点的key和传入的key相等时,返回该节点
                    if (e.hash == hash &&
                            ((k = e.key) == key || (key != null && key.equals(k))))
                        return e;
                } while ((e = e.next) != null);
            }
        }
        // 找不到符合的返回空
        return null;
    }

    /**
     * Returns <tt>true</tt> if this map contains a mapping for the
     * specified key.
     *
     * @param   key   The key whose presence in this map is to be tested
     * @return <tt>true</tt> if this map contains a mapping for the specified
     * key.
     */
    public boolean containsKey(Object key) {
        return getNode(hash(key), key) != null;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public V put(K key, V value) {
        return putVal(hash(key), key, value, false, true);
    }

    /**
     * Implements Map.put and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @param value the value to put
     * @param onlyIfAbsent if true, don't change existing value
     * @param evict if false, the table is in creation mode.
     * @return previous value, or null if none
     */
    //
    final V putVal(int hash, K key, V value, boolean onlyIfAbsent, boolean evict) {

        Node<K,V>[] tab;
        Node<K,V> p;
        int n, i;
        // table是否为空或者length等于0, 如果是则调用resize方法进行初始化;
        // table在第一次使用时进行初始化；
        if ((tab = table) == null || (n = tab.length) == 0)
            //  tab = resize()再散列
            n = (tab = resize()).length;

        // 通过hash值计算索引位置, 如果table表中在该索引位置节点为空则新增一个；
        // 在寻找桶位的时候，这个hash值为与上table的zise-1，同时 将索引位置的头节点赋值给p
        if ((p = tab[i = (n - 1) & hash]) == null)
            tab[i] = newNode(hash, key, value, null);
        else {
            // table表该索引位置不为空
            Node<K,V> e;// 表示插入节点的位置；
            K k;
            // 判断p节点的hash值和key值是否跟传入的hash值和key值相等，如果相等, 则p节点即为要查找的目标节点，赋值给e
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                e = p;
            // 判断p节点是否为TreeNode, 如果是则调用红黑树的putTreeVal方法查找目标节点
            else if (p instanceof TreeNode)
                e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
            else {
                // 走到这代表p节点为普通链表节点
                // 遍历此链表, binCount用于统计节点数
                for (int binCount = 0; ; ++binCount) {
                    //  p.next为空代表不存在目标节点，则新增一个节点插入链表尾部
                    if ((e = p.next) == null) {
                        p.next = newNode(hash, key, value, null);
                        // 计算节点是否超过8个, 减一是因为循环是从p节点的下一个节点开始的计数的
                        if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                            // 如果超过8个，调用treeifyBin方法将该链表转换为红黑树
                            treeifyBin(tab, hash);
                        break;
                    }
                    // e节点的hash值和key值都与传入的相等, 则e即为目标节点,跳出循环
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k))))
                        break;
                    // 将p指向下一个节点
                    p = e;
                }
            }
            // e不为空则代表根据传入的hash值和key值查找到了节点,将该节点的value覆盖,返回oldValue
            if (e != null) { // existing mapping for key
                V oldValue = e.value;
                // onlyIfAbsent只有为空时才put;
                if (!onlyIfAbsent || oldValue == null)
                    e.value = value;
                afterNodeAccess(e);
                return oldValue;
            }
        }
        // 只计算桶数组上的数据
        // 插入节点后超过阈值则进行扩容
        ++modCount;
        if (++size > threshold)
            resize();
        //// 用于LinkedHashMap
        afterNodeInsertion(evict);
        return null;
    }

    /**
     * Initializes or doubles table size.  If null, allocates in
     * accord with initial capacity target held in field threshold.
     * Otherwise, because we are using power-of-two expansion, the
     * elements from each bin must either stay at same index, or move
     * with a power of two offset in the new table.
     *
     * @return the table
     */
    //
    final Node<K,V>[] resize() {

        Node<K,V>[] oldTab = table;

        // 判断是否已经初始化；
        int oldCap = (oldTab == null) ? 0 : oldTab.length;

        int oldThr = threshold;

        int newCap, newThr = 0;

        // 老表的容量大于0，判断老表的容量是否超过最大容量值：如果超过则将阈值设置为Integer.MAX_VALUE;
        // 并直接返回老表（此时oldCap * 2比Integer.MAX_VALUE大，因此无法进行重新分布，只是单纯的将阈值扩容到最大）;
        // 如果容量 * 2小于最大容量并且不小于16，则将阈值设置为原来的两倍;
        if (oldCap > 0) {

            if (oldCap >= MAXIMUM_CAPACITY) {
                // 扩容的条件不会满足了
                threshold = Integer.MAX_VALUE;
                return oldTab;
            }
            // 如果容量*2<最大容量并且>=16, 则将阈值设置为原来的两倍
            else if ((newCap = oldCap << 1) < MAXIMUM_CAPACITY && oldCap >= DEFAULT_INITIAL_CAPACITY)
                newThr = oldThr << 1; // double threshold
        }
        //  老表的容量为0, 老表的阈值大于0, 是因为初始容量被放入阈值
        //  还未初始化table, 但是threshold在初始化容量的时候已被赋值；
        else if (oldThr > 0) // initial capacity was placed in threshold
            newCap = oldThr; // 则将新表的容量设置为老表的阈值
        else {               // zero initial threshold signifies using defaults
            // 老表的容量为0, 老表的阈值为0, 则为空表，设置默认容量和阈值
            // threshold==0 表示使用默认值；
            newCap = DEFAULT_INITIAL_CAPACITY;
            newThr = (int)(DEFAULT_LOAD_FACTOR * DEFAULT_INITIAL_CAPACITY);
        }

        // newThr没有被赋值时；
        // 如果新表的阈值为空, 则通过新的容量*负载因子获得阈值
        if (newThr == 0) {
            float ft = (float)newCap * loadFactor;
            newThr = (newCap < MAXIMUM_CAPACITY && ft < (float)MAXIMUM_CAPACITY ?
                      (int)ft : Integer.MAX_VALUE);
        }
        // 将当前阈值赋值为刚计算出来的新的阈值
        threshold = newThr;
        @SuppressWarnings({"rawtypes","unchecked"})
        // 重新获取// 定义新表,容量为刚计算出来的新容量
        Node<K,V>[] newTab = (Node<K,V>[])new Node[newCap];
        // 将当前的表赋值为新定义的表
        table = newTab;
        // 如果老表不为空, 则需遍历将节点赋值给新表
        if (oldTab != null) {
            //
            for (int j = 0; j < oldCap; ++j) {

                Node<K,V> e;
                // 将索引值为j的老表头节点赋值给e
                if ((e = oldTab[j]) != null) {
                    // 将老表的节点设置为空, 以便垃圾收集器回收空间
                    oldTab[j] = null;
                    // 如果e.next为空, 则代表老表的该位置只有1个节点;
                    // 通过hash值计算新表的索引位置, 直接将该节点放在该位置
                    if (e.next == null)
                        newTab[e.hash & (newCap - 1)] = e;
                    //
                    else if (e instanceof TreeNode)
                        // 调用treeNode的hash分布(跟下面最后一个else的内容几乎相同)
                        ((TreeNode<K,V>)e).split(this, newTab, j, oldCap);
                    else {

                        // preserve order
                        // 存储跟原索引位置相同的节点
                        Node<K,V> loHead = null, loTail = null;
                        // 存储索引位置为:原索引+oldCap的节点
                        Node<K,V> hiHead = null, hiTail = null;
                        Node<K,V> next;

                        do {
                            next = e.next;
                            //如果e 的hash值与老表的容量进行与运算为0,则扩容后的索引位置跟老表的索引位置一样
                            if ((e.hash & oldCap) == 0) {
                                // 进行链表拼接操作：如果loTail为空，代表该节点为第一个节点，则将loHead赋值为该节点；
                                // 否则将节点添加在loTail后面，并将loTail赋值为新增的节点。

                                if (loTail == null)// 如果loTail为空, 代表该节点为第一个节点
                                    loHead = e;// 则将loHead赋值为第一个节点
                                else
                                    loTail.next = e; // 否则将节点添加在loTail后面
                                // 并将loTail赋值为新增的节点
                                loTail = e;
                            }
                            //如果e的hash值与老表的容量进行与运算为1,则扩容后的索引位置为:老表的索引位置＋oldCap
                            else {
                                if (hiTail == null) // 如果hiTail为空, 代表该节点为第一个节点
                                    hiHead = e;// 则将hiHead赋值为第一个节点
                                else
                                    hiTail.next = e;// 否则将节点添加在hiTail后面
                                hiTail = e;// 并将hiTail赋值为新增的节点
                            }
                        } while ((e = next) != null);

                        //  老表节点重新hash分布在新表结束后，如果loTail不为空（说明老表的数据有分布到新表上原索引位置的节点），
                        //  则将最后一个节点的next设为空，并将新表上原索引位置的节点设置为对应的头结点；如果hiTail不为空
                        // （说明老表的数据有分布到新表上原索引+oldCap位置的节点），则将最后一个节点的next设为空，
                        //  并将新表上索引位置为原索引+oldCap的节点设置为对应的头结点。
                        if (loTail != null) {
                            loTail.next = null;// 最后一个节点的next设为空
                            newTab[j] = loHead;// 将原索引位置的节点设置为对应的头结点
                        }
                        if (hiTail != null) {
                            hiTail.next = null;// 最后一个节点的next设为空
                            newTab[j + oldCap] = hiHead;// 将索引位置为原索引+oldCap的节点设置为对应的头结点
                        }
                    }
                }
            }
        }
        return newTab;
    }

    /**
     * Replaces all linked nodes in bin at index for given hash unless
     * table is too small, in which case resizes instead.
     */

    final void treeifyBin(Node<K,V>[] tab, int hash) {

        int n, index;
        Node<K,V> e;
        // table为空 或者 table的长度小于64, 进行扩容
        if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY)
            resize();
        // 根据hash值计算索引值, 遍历该索引位置的链表
        else if ((e = tab[index = (n - 1) & hash]) != null) {

            TreeNode<K,V> hd = null, tl = null;
            do {
                // 调用replacementTreeNode方法（该方法直接返回一个新建的TreeNode）将链表节点转为红黑树节点，
                // 将头结点赋值给hd节点，每次遍历结束将p节点赋值给tl，用于在下一次循环中作为上一个节点进行一些链表的关联操作
                //（p.prev = tl 和 tl.next = p）;
                // 链表节点转红黑树节点
                TreeNode<K,V> p = replacementTreeNode(e, null);
                // tl为空代表为第一次循环
                if (tl == null)
                    // 头结点
                    hd = p;
                else {
                    // 当前节点的prev属性设为上一个节点;
                    // 上一个节点的next属性设置为当前节点
                    p.prev = tl;
                    tl.next = p;
                }
                // tl赋值为p, 在下一次循环中作为上一个节点
                tl = p;
            } while ((e = e.next) != null);
            // 将table该索引位置赋值为新转的TreeNode的头节点
            if ((tab[index] = hd) != null)
                // 以头结点为根结点, 构建红黑树
                hd.treeify(tab);
        }
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings will replace any mappings that this map had for
     * any of the keys currently in the specified map.
     *
     * @param m mappings to be stored in this map
     * @throws NullPointerException if the specified map is null
     */
    public void putAll(Map<? extends K, ? extends V> m) {
        putMapEntries(m, true);
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param  key key whose mapping is to be removed from the map
     * @return the previous value associated with <tt>key</tt>, or
     *         <tt>null</tt> if there was no mapping for <tt>key</tt>.
     *         (A <tt>null</tt> return can also indicate that the map
     *         previously associated <tt>null</tt> with <tt>key</tt>.)
     */
    public V remove(Object key) {
        Node<K,V> e;
        return (e = removeNode(hash(key), key, null, false, true)) == null ?
            null : e.value;
    }

    /**
     * Implements Map.remove and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @param value the value to match if matchValue, else ignored
     * @param matchValue if true only remove if value is equal
     * @param movable if false do not move other nodes while removing
     * @return the node, or null if none
     */

    final Node<K,V> removeNode(int hash, Object key, Object value,
                               boolean matchValue, boolean movable) {

        Node<K,V>[] tab;
        Node<K,V> p;
        int n, index;
        // 如果table不为空并且根据hash值计算出来的索引位置不为空, 将该位置的节点赋值给p
        //
        if ((tab = table) != null && (n = tab.length) > 0 &&
            (p = tab[index = (n - 1) & hash]) != null) {
            //
            Node<K,V> node = null, e; K k; V v;
            // 如果p的hash值和key都与入参的相同, 则p即为目标节点, 赋值给node
            if (p.hash == hash &&
                ((k = p.key) == key || (key != null && key.equals(k))))
                node = p;

            else if ((e = p.next) != null) {
                // 否则向下遍历节点
                if (p instanceof TreeNode)
                    // 如果p是TreeNode则调用红黑树的方法查找节点
                    node = ((TreeNode<K,V>)p).getTreeNode(hash, key);
                else {
                    do {
                        // 遍历链表查找符合条件的节点
                        // 当节点的hash值和key与传入的相同,则该节点即为目标节点
                        if (e.hash == hash &&
                            ((k = e.key) == key ||
                             (key != null && key.equals(k)))) {
                            node = e;// 赋值给node, 并跳出循环
                            break;
                        }
                        p = e;// p节点赋值为本次结束的e
                    } while ((e = e.next) != null);
                }
            }

            // 如果 node不为空(即根据传入key和hash值查找到目标节点)，则进行移除操作
            if (node != null && (!matchValue || (v = node.value) == value ||
                                 (value != null && value.equals(v)))) {
                if (node instanceof TreeNode)
                    // 如果是TreeNode则调用红黑树的移除方法
                    ((TreeNode<K,V>)node).removeTreeNode(this, tab, movable);
                // 走到这代表节点是普通链表节点
                // 如果node是该索引位置的头结点则直接将该索引位置的值赋值为node的next节点
                else if (node == p)
                    tab[index] = node.next;
                else
                    // 否则将node的上一个节点的next属性设置为node的next节点,
                    // 即将node节点移除, 将node的上下节点进行关联(链表的移除)
                    p.next = node.next;
                ++modCount;// 修改次数+1
                --size;// table的总节点数-1
                afterNodeRemoval(node);
                return node;
            }
        }
        return null;
    }

    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
        Node<K,V>[] tab;
        modCount++;
        if ((tab = table) != null && size > 0) {
            size = 0;
            for (int i = 0; i < tab.length; ++i)
                tab[i] = null;
        }
    }

    /**
     * Returns <tt>true</tt> if this map maps one or more keys to the
     * specified value.
     *
     * @param value value whose presence in this map is to be tested
     * @return <tt>true</tt> if this map maps one or more keys to the
     *         specified value
     */
    public boolean containsValue(Object value) {
        Node<K,V>[] tab; V v;
        if ((tab = table) != null && size > 0) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    if ((v = e.value) == value ||
                        (value != null && value.equals(v)))
                        return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * <tt>Iterator.remove</tt>, <tt>Set.remove</tt>,
     * <tt>removeAll</tt>, <tt>retainAll</tt>, and <tt>clear</tt>
     * operations.  It does not support the <tt>add</tt> or <tt>addAll</tt>
     * operations.
     *
     * @return a set view of the keys contained in this map
     */
    public Set<K> keySet() {
        Set<K> ks = keySet;
        if (ks == null) {
            ks = new KeySet();
            keySet = ks;
        }
        return ks;
    }

    final class KeySet extends AbstractSet<K> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<K> iterator()     { return new KeyIterator(); }
        public final boolean contains(Object o) { return containsKey(o); }
        public final boolean remove(Object key) {
            return removeNode(hash(key), key, null, false, true) != null;
        }
        public final Spliterator<K> spliterator() {
            return new KeySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super K> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.key);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     * The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own <tt>remove</tt> operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Collection.remove</tt>, <tt>removeAll</tt>,
     * <tt>retainAll</tt> and <tt>clear</tt> operations.  It does not
     * support the <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a view of the values contained in this map
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;
        }
        return vs;
    }

    final class Values extends AbstractCollection<V> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<V> iterator()     { return new ValueIterator(); }
        public final boolean contains(Object o) { return containsValue(o); }
        public final Spliterator<V> spliterator() {
            return new ValueSpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super V> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e.value);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     * The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own <tt>remove</tt> operation, or through the
     * <tt>setValue</tt> operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the <tt>Iterator.remove</tt>,
     * <tt>Set.remove</tt>, <tt>removeAll</tt>, <tt>retainAll</tt> and
     * <tt>clear</tt> operations.  It does not support the
     * <tt>add</tt> or <tt>addAll</tt> operations.
     *
     * @return a set view of the mappings contained in this map
     */
    // entrySet为空 则返回 new EntrySet())；
    public Set<Map.Entry<K,V>> entrySet() {
        Set<Map.Entry<K,V>> es;
        return (es = entrySet) == null ? (entrySet = new EntrySet()) : es;
    }

    final class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public final int size()                 { return size; }
        public final void clear()               { HashMap.this.clear(); }
        public final Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator();
        }
        public final boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>) o;
            Object key = e.getKey();
            Node<K,V> candidate = getNode(hash(key), key);
            return candidate != null && candidate.equals(e);
        }
        public final boolean remove(Object o) {
            if (o instanceof Map.Entry) {
                Map.Entry<?,?> e = (Map.Entry<?,?>) o;
                Object key = e.getKey();
                Object value = e.getValue();
                return removeNode(hash(key), key, value, true, true) != null;
            }
            return false;
        }
        public final Spliterator<Map.Entry<K,V>> spliterator() {
            return new EntrySpliterator<>(HashMap.this, 0, -1, 0, 0);
        }
        public final void forEach(Consumer<? super Map.Entry<K,V>> action) {
            Node<K,V>[] tab;
            if (action == null)
                throw new NullPointerException();
            if (size > 0 && (tab = table) != null) {
                int mc = modCount;
                for (int i = 0; i < tab.length; ++i) {
                    for (Node<K,V> e = tab[i]; e != null; e = e.next)
                        action.accept(e);
                }
                if (modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }
    }

    // Overrides of JDK8 Map extension methods

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        Node<K,V> e;
        return (e = getNode(hash(key), key)) == null ? defaultValue : e.value;
    }

    @Override
    public V putIfAbsent(K key, V value) {
        return putVal(hash(key), key, value, true, true);
    }

    @Override
    public boolean remove(Object key, Object value) {
        return removeNode(hash(key), key, value, true, true) != null;
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Node<K,V> e; V v;
        if ((e = getNode(hash(key), key)) != null &&
            ((v = e.value) == oldValue || (v != null && v.equals(oldValue)))) {
            e.value = newValue;
            afterNodeAccess(e);
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, V value) {
        Node<K,V> e;
        if ((e = getNode(hash(key), key)) != null) {
            V oldValue = e.value;
            e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
        return null;
    }

    @Override
    public V computeIfAbsent(K key,
                             Function<? super K, ? extends V> mappingFunction) {
        if (mappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
            V oldValue;
            if (old != null && (oldValue = old.value) != null) {
                afterNodeAccess(old);
                return oldValue;
            }
        }
        V v = mappingFunction.apply(key);
        if (v == null) {
            return null;
        } else if (old != null) {
            old.value = v;
            afterNodeAccess(old);
            return v;
        }
        else if (t != null)
            t.putTreeVal(this, tab, hash, key, v);
        else {
            tab[i] = newNode(hash, key, v, first);
            if (binCount >= TREEIFY_THRESHOLD - 1)
                treeifyBin(tab, hash);
        }
        ++modCount;
        ++size;
        afterNodeInsertion(true);
        return v;
    }

    public V computeIfPresent(K key,
                              BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        Node<K,V> e; V oldValue;
        int hash = hash(key);
        if ((e = getNode(hash, key)) != null &&
            (oldValue = e.value) != null) {
            V v = remappingFunction.apply(key, oldValue);
            if (v != null) {
                e.value = v;
                afterNodeAccess(e);
                return v;
            }
            else
                removeNode(hash, key, null, false, true);
        }
        return null;
    }

    @Override
    public V compute(K key,
                     BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        V oldValue = (old == null) ? null : old.value;
        V v = remappingFunction.apply(key, oldValue);
        if (old != null) {
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            else
                removeNode(hash, key, null, false, true);
        }
        else if (v != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, v);
            else {
                tab[i] = newNode(hash, key, v, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return v;
    }

    @Override
    public V merge(K key, V value,
                   BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        if (value == null)
            throw new NullPointerException();
        if (remappingFunction == null)
            throw new NullPointerException();
        int hash = hash(key);
        Node<K,V>[] tab; Node<K,V> first; int n, i;
        int binCount = 0;
        TreeNode<K,V> t = null;
        Node<K,V> old = null;
        if (size > threshold || (tab = table) == null ||
            (n = tab.length) == 0)
            n = (tab = resize()).length;
        if ((first = tab[i = (n - 1) & hash]) != null) {
            if (first instanceof TreeNode)
                old = (t = (TreeNode<K,V>)first).getTreeNode(hash, key);
            else {
                Node<K,V> e = first; K k;
                do {
                    if (e.hash == hash &&
                        ((k = e.key) == key || (key != null && key.equals(k)))) {
                        old = e;
                        break;
                    }
                    ++binCount;
                } while ((e = e.next) != null);
            }
        }
        if (old != null) {
            V v;
            if (old.value != null)
                v = remappingFunction.apply(old.value, value);
            else
                v = value;
            if (v != null) {
                old.value = v;
                afterNodeAccess(old);
            }
            else
                removeNode(hash, key, null, false, true);
            return v;
        }
        if (value != null) {
            if (t != null)
                t.putTreeVal(this, tab, hash, key, value);
            else {
                tab[i] = newNode(hash, key, value, first);
                if (binCount >= TREEIFY_THRESHOLD - 1)
                    treeifyBin(tab, hash);
            }
            ++modCount;
            ++size;
            afterNodeInsertion(true);
        }
        return value;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Node<K,V>[] tab;
        if (action == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next)
                    action.accept(e.key, e.value);
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Node<K,V>[] tab;
        if (function == null)
            throw new NullPointerException();
        if (size > 0 && (tab = table) != null) {
            int mc = modCount;
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    e.value = function.apply(e.key, e.value);
                }
            }
            if (modCount != mc)
                throw new ConcurrentModificationException();
        }
    }

    /* ------------------------------------------------------------ */
    // Cloning and serialization

    /**
     * Returns a shallow copy of this <tt>HashMap</tt> instance: the keys and
     * values themselves are not cloned.
     *
     * @return a shallow copy of this map
     */
    @SuppressWarnings("unchecked")
    @Override
    public Object clone() {
        HashMap<K,V> result;
        try {
            result = (HashMap<K,V>)super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
        result.reinitialize();
        result.putMapEntries(this, false);
        return result;
    }

    // These methods are also used when serializing HashSets
    final float loadFactor() { return loadFactor; }
    final int capacity() {
        return (table != null) ? table.length :
            (threshold > 0) ? threshold :
            DEFAULT_INITIAL_CAPACITY;
    }

    /**
     * Save the state of the <tt>HashMap</tt> instance to a stream (i.e.,
     * serialize it).
     *
     * @serialData The <i>capacity</i> of the HashMap (the length of the
     *             bucket array) is emitted (int), followed by the
     *             <i>size</i> (an int, the number of key-value
     *             mappings), followed by the key (Object) and value (Object)
     *             for each key-value mapping.  The key-value mappings are
     *             emitted in no particular order.
     */
    private void writeObject(java.io.ObjectOutputStream s)
        throws IOException {
        int buckets = capacity();
        // Write out the threshold, loadfactor, and any hidden stuff
        s.defaultWriteObject();
        s.writeInt(buckets);
        s.writeInt(size);
        internalWriteEntries(s);
    }

    /**
     * Reconstitute the {@code HashMap} instance from a stream (i.e.,
     * deserialize it).
     */
    private void readObject(java.io.ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        // Read in the threshold (ignored), loadfactor, and any hidden stuff
        s.defaultReadObject();
        reinitialize();
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new InvalidObjectException("Illegal load factor: " +
                                             loadFactor);
        s.readInt();                // Read and ignore number of buckets
        int mappings = s.readInt(); // Read number of mappings (size)
        if (mappings < 0)
            throw new InvalidObjectException("Illegal mappings count: " +
                                             mappings);
        else if (mappings > 0) { // (if zero, use defaults)
            // Size the table using given load factor only if within
            // range of 0.25...4.0
            float lf = Math.min(Math.max(0.25f, loadFactor), 4.0f);
            float fc = (float)mappings / lf + 1.0f;
            int cap = ((fc < DEFAULT_INITIAL_CAPACITY) ?
                       DEFAULT_INITIAL_CAPACITY :
                       (fc >= MAXIMUM_CAPACITY) ?
                       MAXIMUM_CAPACITY :
                       tableSizeFor((int)fc));
            float ft = (float)cap * lf;
            threshold = ((cap < MAXIMUM_CAPACITY && ft < MAXIMUM_CAPACITY) ?
                         (int)ft : Integer.MAX_VALUE);
            @SuppressWarnings({"rawtypes","unchecked"})
                Node<K,V>[] tab = (Node<K,V>[])new Node[cap];
            table = tab;

            // Read the keys and values, and put the mappings in the HashMap
            for (int i = 0; i < mappings; i++) {
                @SuppressWarnings("unchecked")
                    K key = (K) s.readObject();
                @SuppressWarnings("unchecked")
                    V value = (V) s.readObject();
                putVal(hash(key), key, value, false, false);
            }
        }
    }

    /* ------------------------------------------------------------ */
    // iterators

    abstract class HashIterator {
        Node<K,V> next;        // next entry to return
        Node<K,V> current;     // current entry
        int expectedModCount;  // for fast-fail
        int index;             // current slot

        HashIterator() {
            expectedModCount = modCount;
            Node<K,V>[] t = table;
            current = next = null;
            index = 0;
            if (t != null && size > 0) { // advance to first entry
                do {} while (index < t.length && (next = t[index++]) == null);
            }
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Node<K,V> nextNode() {
            Node<K,V>[] t;
            Node<K,V> e = next;
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            if (e == null)
                throw new NoSuchElementException();
            if ((next = (current = e).next) == null && (t = table) != null) {
                do {} while (index < t.length && (next = t[index++]) == null);
            }
            return e;
        }

        public final void remove() {
            Node<K,V> p = current;
            if (p == null)
                throw new IllegalStateException();
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            current = null;
            K key = p.key;
            removeNode(hash(key), key, null, false, false);
            expectedModCount = modCount;
        }
    }

    final class KeyIterator extends HashIterator
        implements Iterator<K> {
        public final K next() { return nextNode().key; }
    }

    final class ValueIterator extends HashIterator
        implements Iterator<V> {
        public final V next() { return nextNode().value; }
    }

    final class EntryIterator extends HashIterator
        implements Iterator<Map.Entry<K,V>> {
        public final Map.Entry<K,V> next() { return nextNode(); }
    }

    /* ------------------------------------------------------------ */
    // spliterators

    static class HashMapSpliterator<K,V> {
        final HashMap<K,V> map;
        Node<K,V> current;          // current node
        int index;                  // current index, modified on advance/split
        int fence;                  // one past last index
        int est;                    // size estimate
        int expectedModCount;       // for comodification checks

        HashMapSpliterator(HashMap<K,V> m, int origin,
                           int fence, int est,
                           int expectedModCount) {
            this.map = m;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() { // initialize fence and size on first use
            int hi;
            if ((hi = fence) < 0) {
                HashMap<K,V> m = map;
                est = m.size;
                expectedModCount = m.modCount;
                Node<K,V>[] tab = m.table;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            return hi;
        }

        public final long estimateSize() {
            getFence(); // force init
            return (long) est;
        }
    }

    static final class KeySpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<K> {
        KeySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                       int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new KeySpliterator<>(map, lo, index = mid, est >>>= 1,
                                        expectedModCount);
        }

        public void forEachRemaining(Consumer<? super K> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.key);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        K k = current.key;
                        current = current.next;
                        action.accept(k);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT;
        }
    }

    static final class ValueSpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<V> {
        ValueSpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new ValueSpliterator<>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super V> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p.value);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        V v = current.value;
                        current = current.next;
                        action.accept(v);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0);
        }
    }

    static final class EntrySpliterator<K,V>
        extends HashMapSpliterator<K,V>
        implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(HashMap<K,V> m, int origin, int fence, int est,
                         int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K,V> trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid || current != null) ? null :
                new EntrySpliterator<>(map, lo, index = mid, est >>>= 1,
                                          expectedModCount);
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K,V>> action) {
            int i, hi, mc;
            if (action == null)
                throw new NullPointerException();
            HashMap<K,V> m = map;
            Node<K,V>[] tab = m.table;
            if ((hi = fence) < 0) {
                mc = expectedModCount = m.modCount;
                hi = fence = (tab == null) ? 0 : tab.length;
            }
            else
                mc = expectedModCount;
            if (tab != null && tab.length >= hi &&
                (i = index) >= 0 && (i < (index = hi) || current != null)) {
                Node<K,V> p = current;
                current = null;
                do {
                    if (p == null)
                        p = tab[i++];
                    else {
                        action.accept(p);
                        p = p.next;
                    }
                } while (p != null || i < hi);
                if (m.modCount != mc)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            int hi;
            if (action == null)
                throw new NullPointerException();
            Node<K,V>[] tab = map.table;
            if (tab != null && tab.length >= (hi = getFence()) && index >= 0) {
                while (current != null || index < hi) {
                    if (current == null)
                        current = tab[index++];
                    else {
                        Node<K,V> e = current;
                        current = current.next;
                        action.accept(e);
                        if (map.modCount != expectedModCount)
                            throw new ConcurrentModificationException();
                        return true;
                    }
                }
            }
            return false;
        }

        public int characteristics() {
            return (fence < 0 || est == map.size ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT;
        }
    }

    /* ------------------------------------------------------------ */
    // LinkedHashMap support


    /*
     * The following package-protected methods are designed to be
     * overridden by LinkedHashMap, but not by any other subclass.
     * Nearly all other internal methods are also package-protected
     * but are declared final, so can be used by LinkedHashMap, view
     * classes, and HashSet.
     */

    // Create a regular (non-tree) node
    Node<K,V> newNode(int hash, K key, V value, Node<K,V> next) {
        return new Node<>(hash, key, value, next);
    }

    // For conversion from TreeNodes to plain nodes
    Node<K,V> replacementNode(Node<K,V> p, Node<K,V> next) {
        return new Node<>(p.hash, p.key, p.value, next);
    }

    // Create a tree bin node
    TreeNode<K,V> newTreeNode(int hash, K key, V value, Node<K,V> next) {
        return new TreeNode<>(hash, key, value, next);
    }

    // For treeifyBin
    TreeNode<K,V> replacementTreeNode(Node<K,V> p, Node<K,V> next) {
        return new TreeNode<>(p.hash, p.key, p.value, next);
    }

    /**
     * Reset to initial default state.  Called by clone and readObject.
     */
    void reinitialize() {
        table = null;
        entrySet = null;
        keySet = null;
        values = null;
        modCount = 0;
        threshold = 0;
        size = 0;
    }

    // Callbacks to allow LinkedHashMap post-actions
    void afterNodeAccess(Node<K,V> p) { }
    void afterNodeInsertion(boolean evict) { }
    void afterNodeRemoval(Node<K,V> p) { }

    // Called only from writeObject, to ensure compatible ordering.
    void internalWriteEntries(java.io.ObjectOutputStream s) throws IOException {
        Node<K,V>[] tab;
        if (size > 0 && (tab = table) != null) {
            for (int i = 0; i < tab.length; ++i) {
                for (Node<K,V> e = tab[i]; e != null; e = e.next) {
                    s.writeObject(e.key);
                    s.writeObject(e.value);
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    // Tree bins

    /**
     * Entry for Tree bins. Extends LinkedHashMap.Entry (which in turn
     * extends Node) so can be used as extension of either regular or
     * linked node.
     */
    // 红黑树节点
    static final class TreeNode<K,V> extends LinkedHashMap.Entry<K,V> {
        //
        TreeNode<K,V> parent;  // red-black tree links

        TreeNode<K,V> left;

        TreeNode<K,V> right;
        // next
        TreeNode<K,V> prev;    // needed to unlink next upon deletion

        boolean red;
        //
        TreeNode(int hash, K key, V val, Node<K,V> next) {
            super(hash, key, val, next);
        }

        /**
         * Returns root of tree containing this node.
         */
        // 找到树的根节点；
        final TreeNode<K,V> root() {
            // TreeNode<K,V> r = this;TreeNode<K,V> p
            for (TreeNode<K,V> r = this, p;;) {
                //
                if ((p = r.parent) == null)
                    return r;
                r = p;
            }
        }

        /**
         * Ensures that the given root is the first node of its bin.
         */
        // 如果当前索引位置的头节点不是root节点, 对链表进行调整：
        //    将root放到头节点的位置, root节点从链表中截取出来，放在开头；
        static <K,V> void moveRootToFront(Node<K,V>[] tab, TreeNode<K,V> root) {
            int n;

            if (root != null && tab != null && (n = tab.length) > 0) {

                // 根据root节点的hash值计算出索引位置，判断该索引位置的头节点是否为root节点，
                // 如果不是则进行以下操作将该索引位置的头结点替换为root节点。

                // 需要改变在table中的位置； root和this的hash code相同；
                int index = (n - 1) & root.hash;
                // first需要
                TreeNode<K,V> first = (TreeNode<K,V>)tab[index];
                // 如果root节点不是该索引位置的头节点；
                if (root != first) {

                    Node<K,V> rn;
                    tab[index] = root; // 将该索引位置的头节点赋值为root节点
                    TreeNode<K,V> rp = root.prev; // root节点的上一个节点
                    // 如果root节点的下一个节点不为空,
                    // 则将root节点的下一个节点的prev属性设置为root节点的上一个节点
                    // 3、 如果root节点的next节点不为空，则将root节点的next节点的prev属性设置为root节点的prev节点。
                    if ((rn = root.next) != null)
                        ((TreeNode<K,V>)rn).prev = rp;
                    // 如果root节点的上一个节点不为空,
                    // 则将root节点的上一个节点的next属性设置为root节点的下一个节点
                    // 4 如果root节点的prev节点不为空，则将root节点的prev节点的next属性设置为root节点的next节点
                    // （3和4两个操作是一个完整的链表移除某个节点过程）。
                    if (rp != null)
                        rp.next = rn;
                    // 5 如果原头节点不为空，则将原头节点的prev属性设置为root节点
                    if (first != null)
                        first.prev = root;
                    // 将root节点的next属性设置为原头节点
                    //6 将root节点的next属性设置为原头节点
                    // 5和6两个操作将first节点接到root节点后面）
                    root.next = first;
                    // root此时已经被放到该位置的头结点位置，因此将prev属性设为空。
                    root.prev = null;
                }
                // 检查树是否正常
                assert checkInvariants(root);
            }
        }

        /**
         * Finds the node starting at root p with the given hash and key.
         * The kc argument caches comparableClassFor(key) upon first use
         * comparing keys.
         */

        // 从调用此方法的结点为root，开始查找, 通过hash值和key找到对应的节点;
        // 此处是红黑树的遍历, 红黑树是特殊的自平衡二叉查找树;
        // 平衡二叉查找树的特点：左节点<根节点<右节点;
        final TreeNode<K,V> find(int h, Object k, Class<?> kc) {
            // this为调用此方法的 root 节点.

            TreeNode<K,V> p = this;//
            do {
                int ph, dir; K pk;

                TreeNode<K,V> pl = p.left, pr = p.right, q;

                // 传入的hash值h小于p节点this的hash值, 则p节点往左边遍历；
                if ((ph = p.hash) > h)
                    p = pl;  // p赋值为p节点的左节点
                // 传入的hash值大于p节点的hash值, 则往p节点的右边遍历
                else if (ph < h)
                    p = pr;  // p赋值为p节点的右节点
                // 当ph = p.hash时；向下执行；----------------------------------------------------------------------------
                    // 检测key是否一致；

                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    //传入的hash值和key值等于p节点的hash值和key值,则p节点为目标节点,返回p节点
                    return p;
                // 当ph = p.hash时，且key不一致；
                    // 当ph = p.hash时；key不一致时，需要选择一个方向进行遍历；默认为右-----------------------------------------
                else if (pl == null)// p节点的左节点为空则将向右遍历
                    p = pr;
                    // 当ph = p.hash时，且key不一致；
                else if (pr == null)
                    // p节点的右节点为空则向左遍历
                    p = pl;

                // 当 ph = p.hash时，且key不一致， 且不能判断遍历方向时；
                // 则根据Comparable方法进行比较；
                else if ((kc != null ||
                          // 如果传入的key所属的类实现了Comparable接口, 则将传入的key跟p节点的key比较
                          (kc = comparableClassFor(k)) != null) &&  // 此行不为空代表k实现了Comparable
                         (dir = compareComparables(kc, k, pk)) != 0)  //k<pk则dir<0, k>pk则dir>0
                    // k < pk则向左遍历(p赋值为p的左节点), 否则向右遍历
                    p = (dir < 0) ? pl : pr;
                //代码走到此处，代表key所属类没有实现Comparable，因此直接指定向p的右边遍历，如果能找到目标节点则返回
                else if ((q = pr.find(h, k, kc)) != null)
                    return q;
                else
                    // 代码走到此处代表上一个向右遍历（pr.find(h, k, kc)）为空, 因此直接向左遍历
                    p = pl;
            } while (p != null);
            //此处，代表key所属类没有实现Comparable，因
            return null;
        }

        /**
         * Calls find for root node.
         */


        // 找到调用此方法的节点的树的根节点
        final TreeNode<K,V> getTreeNode(int h, Object k) {
            // root ()找到调用此方法的节点的树的根节点
            // 使用该树的根节点调用find方法（见下文代码块2）
            // 当parent == null时，this为root；否则执行root()
            return ((parent != null) ? root() : this).find(h, k, null);
        }

        /**
         * Tie-breaking utility for ordering insertions when equal
         * hashCodes and non-comparable. We don't require a total
         * order, just a consistent insertion rule to maintain
         * equivalence across rebalancings. Tie-breaking further than
         * necessary simplifies testing a bit.
         */
        // 用于不可比较或者hashCode相同时进行比较的方法, 只是一个一致的插入规则，用来维护重定位的等价性。
        // 定义一套规则用于极端情况下比较两个参数的大小
        static int tieBreakOrder(Object a, Object b) {
            int d;
            if (a == null || b == null ||
                (d = a.getClass().getName().
                 compareTo(b.getClass().getName())) == 0)
                d = (System.identityHashCode(a) <= System.identityHashCode(b) ?
                     -1 : 1);
            return d;
        }

        /**
         * Forms tree of the nodes linked from this node.
         * @return root of tree
         */
        // 构建红黑树
        //
        final void treeify(Node<K,V>[] tab) {
            TreeNode<K,V> root = null;
            // this即为调用此方法的TreeNode
            // 从调用此方法的节点作为起点，开始进行遍历，并将此节点设为root节点，标记为黑色（x.red = false）。
            for (TreeNode<K,V> x = this, next; x != null; x = next) {
                // next赋值为x的下个节点
                next = (TreeNode<K,V>)x.next;
                // 将x的左右节点设置为空
                x.left = x.right = null;
                // 如果还没有根结点, 则将x设置为根结点;
                if (root == null) {
                    // 根结点没有父节点
                    x.parent = null;
                    // 根结点必须为黑色
                    x.red = false;
                    // 将x设置为根结点
                    root = x;
                }
                else {

                    K k = x.key;// k赋值为x的key
                    int h = x.hash;// h赋值为x的hash值

                    Class<?> kc = null;
                    // 如果当前节点x不是根结点, 则从根节点开始查找属于该节点的位置
                    for (TreeNode<K,V> p = root;;) {
                        int dir, ph;
                        K pk = p.key;

                        if ((ph = p.hash) > h)// 如果x节点的hash值小于p节点的hash值
                            dir = -1;// 则将dir赋值为-1, 代表向p的左边查找
                        else if (ph < h)// 与上面相反, 如果x节点的hash值大于p节点的hash值
                            dir = 1; // 则将dir赋值为1, 代表向p的右边查找
                        // 走到这代表x的hash值和p的hash值相等，则比较key值
                        else if ((kc == null &&// 如果k没有实现Comparable接口 或者 x节点的key和p节点的key相等
                                  (kc = comparableClassFor(k)) == null) ||
                                 (dir = compareComparables(kc, k, pk)) == 0)
                            // 使用定义的一套规则来比较x节点和p节点的大小，用来决定向左还是向右查找
                            dir = tieBreakOrder(k, pk);

                        TreeNode<K,V> xp = p;// xp赋值为x的父节点,中间变量用于下面给x的父节点赋值
                        // dir<=0 则向p左边查找,否则向p右边查找,如果为null,则代表该位置即为x的目标位置
                        if ((p = (dir <= 0) ? p.left : p.right) == null) {
                            x.parent = xp; // x的父节点即为最后一次遍历的p节点
                            if (dir <= 0)// 如果时dir <= 0, 则代表x节点为父节点的左节点
                                xp.left = x;
                            else// 如果时dir > 0, 则代表x节点为父节点的右节点
                                xp.right = x;
                            // 进行红黑树的插入平衡(通过左旋、右旋和改变节点颜色来保证当前树符合红黑树的要求)
                            root = balanceInsertion(root, x);
                            break;
                        }
                    }
                }
            }
            // 如果root节点不在table索引位置的头结点, 则将其调整为头结点
            moveRootToFront(tab, root);
        }

        /**
         * Returns a list of non-TreeNodes replacing those linked from
         * this node.
         */

        // // 将红黑树节点转为链表节点, 当节点<=6个时会被触发
        final Node<K,V> untreeify(HashMap<K,V> map) {
            Node<K,V> hd = null, tl = null;// hd指向头结点, tl指向尾节点
            // 从调用该方法的节点, 即链表的头结点开始遍历, 将所有节点全转为链表节点
            for (Node<K,V> q = this; q != null; q = q.next) {
                // 调用replacementNode方法构建链表节点
                Node<K,V> p = map.replacementNode(q, null);
                // 如果tl为null, 则代表当前节点为第一个节点, 将hd赋值为该节点
                if (tl == null)
                    hd = p;
                else// 否则, 将尾节点的next属性设置为当前节点p
                    tl.next = p;
                tl = p;// 每次都将tl节点指向当前节点, 即尾节点
            }
            return hd;// 返回转换后的链表的头结点
        }

        /**
         * Tree version of putVal.
         */
        // 红黑树插入会同时维护原来的链表属性, 即原来的next属性；
        final TreeNode<K,V> putTreeVal(HashMap<K,V> map, Node<K,V>[] tab, int h, K k, V v) {


            Class<?> kc = null;

            boolean searched = false;
            // 查找根节点, 索引位置的头节点并不一定为红黑树的根结点
            TreeNode<K,V> root = (parent != null) ? root() : this;

            for (TreeNode<K,V> p = root;;) {  // 将根节点赋值给p, 开始遍历
                int dir, ph;
                K pk;

                // 如果传入的hash值小于p节点的hash值
                if ((ph = p.hash) > h)
                    dir = -1;// 则将dir赋值为-1, 代表向p的左边查找树
                // 如果传入的hash值大于p节点的hash值,
                else if (ph < h)
                    dir = 1;// 则将dir赋值为1, 代表向p的右边查找树
                // 如果传入的hash值和key值等于p节点的hash值和key值, 则p节点即为目标节点, 返回p节点
                else if ((pk = p.key) == k || (k != null && k.equals(pk)))
                    return p;
                // 如果k所属的类没有实现Comparable接口 或者 k和p节点的key相等

                //如果k所属的类没有实现Comparable接口，或者k和p节点的key使用compareTo方法比较相等：
                    // 第一次会从p节点的左节点和右节点分别调用find方法（见上文代码块2）进行查找，如果查找到目标节点则返回；
                    // 如果不是第一次或者调用find方法没有找到目标节点，则调用tieBreakOrder方法（见下文代码块5）比较k和p节点的key值的大小，
                    // 以决定向树的左节点还是右节点查找。


                else if ((kc == null && (kc = comparableClassFor(k)) == null) ||
                         (dir = compareComparables(kc, k, pk)) == 0) {
                    if (!searched) {
                        TreeNode<K,V> q, ch;
                        searched = true;
                        if (((ch = p.left) != null &&
                             (q = ch.find(h, k, kc)) != null) ||
                            ((ch = p.right) != null &&
                             (q = ch.find(h, k, kc)) != null))
                            return q;
                    }
                    dir = tieBreakOrder(k, pk);
                }
                // xp赋值为x的父节点,中间变量,用于下面给x的父节点赋值
                TreeNode<K,V> xp = p;
                //  p 的迭代赋值是在p = (dir <= 0) ? p.left : p.right中发生的；
                // 如果dir <= 0则向左节点查找（p赋值为p.left，并进行下一次循环），否则向右节点查找，
                // 如果已经无法继续查找（p赋值后为null），则代表该位置即为x的目标位置，
                // 另外变量xp用来记录查找的最后一个节点，即下文新增的x节点的父节点。

                /*
                * 以传入的hash、key、value参数和xp节点的next节点为参数构建x节点
                * （注意：xp节点在此处可能是叶子节点、没有左节点的节点、没有右节点的节点三种情况，
                * 即使它是叶子节点，它也可能有next节点，红黑树的结构跟链表的结构是互不影响的，
                * 不会因为某个节点是叶子节点就说它没有next节点，
                * 红黑树在进行操作时会同时维护红黑树结构和链表结构，next属性就是用来维护链表结构的），
                * 根据dir的值决定x决定放在xp节点的左节点还是右节点，将xp的next节点设为x，
                * 将x的parent和prev节点设为xp，如果原xp的next节点（xpn）不为空, 则将该节点的prev节点设置为x节点,
                * 与上面的将x节点的next节点设置为xpn对应。
                * */


                if ((p = (dir <= 0) ? p.left : p.right) == null) {
                    // 走进来代表已经找到x的位置，只需将x放到该位置即可；
                    // xpn用于维护链表顺序；
                    Node<K,V> xpn = xp.next;// xp的next节点
                    // 创建新的节点, 其中x的next节点为xpn, 即将x节点插入xp与xpn之间
                    TreeNode<K,V> x = map.newTreeNode(h, k, v, xpn);
                    if (dir <= 0) // 如果时dir <= 0, 则代表x节点为xp的左节点
                        xp.left = x;
                    else // 如果时dir> 0, 则代表x节点为xp的右节点
                        xp.right = x;
                    xp.next = x;// 将xp的next节点设置为
                    x.parent = x.prev = xp; //// 将x的parent和prev节点设置为xp
                    // 如果xpn不为空,则将xpn的prev节点设置为x节点,与上文的x节点的next节点对应
                    if (xpn != null)
                        ((TreeNode<K,V>)xpn).prev = x;
                    // 进行红黑树的插入平衡调整
                    moveRootToFront(tab, balanceInsertion(root, x));
                    return null;
                }
            }
        }

        /**
         * Removes the given node, that must be present before this call.
         * This is messier than typical red-black deletion code because we
         * cannot swap the contents of an interior node with a leaf
         * successor that is pinned by "next" pointers that are accessible
         * independently during traversal. So instead we swap the tree
         * linkages. If the current tree appears to have too few nodes,
         * the bin is converted back to a plain bin. (The test triggers
         * somewhere between 2 and 6 nodes, depending on tree structure).
         */
        final void removeTreeNode(HashMap<K,V> map, Node<K,V>[] tab,
                                  boolean movable) {
            // 链表的处理start
            int n;
            // table为空或者length为0直接返回
            if (tab == null || (n = tab.length) == 0)
                return;
            // 根据hash计算出索引的位置
            int index = (n - 1) & hash;

            // 索引位置的头结点赋值给first和root
            TreeNode<K,V> first = (TreeNode<K,V>)tab[index],
                    root = first, rl;

            // 此方法的this为要被移除node节点,
            // 此处next即为node的next节点, prev即为node的prev节点；
            TreeNode<K,V> succ = (TreeNode<K,V>)next, pred = prev;
            // 如果node节点的prev节点为空
            if (pred == null)
                // 则将table索引位置的值和first节点的值赋值为succ节点(node的next节点)即可；
                tab[index] = first = succ;
            else
                // 否则将node的prev节点的next属性设置为succ节点(node的next节点)(链表的移除)
                pred.next = succ;
            // 如果succ节点不为空
            if (succ != null)
                succ.prev = pred;// 则将succ的prev节点设置为pred, 与上面对应
            // 如果此处first为空, 则代表该索引位置已经没有节点则直接返回
            if (first == null)
                return;
            // 链表的处理end


            // 如果 root 的父节点不为空, 则将root赋值为根结点
            // (root在上面被赋值为索引位置的头结点, 索引位置的头节点并不一定为红黑树的根结点)
            if (root.parent != null)
                root = root.root();

            // 通过root节点来判断此红黑树是否太小, 如果是则调用untreeify方法转为链表节点并返回
            // (转链表后就无需再进行下面的红黑树处理)
            if (root == null || root.right == null || // root.right == null 或 root.left == null 表示每个路径都只有一个黑点，root.left.left=null 表示每个路径不超过2个黑点；
                (rl = root.left) == null || rl.left == null) {
                tab[index] = first.untreeify(map);  // too small
                return;
            }



            // 以下代码为红黑树的处理,
            // 上面已经说了this为要被移除的node节点；
            // 将p赋值为node节点,pl赋值为node的左节点,pr赋值为node的右节点
            //replacement变量用来存储将要替换掉被移除的node节点。
            TreeNode<K,V> p = this, pl = left, pr = right, replacement;

            //
            if (pl != null && pr != null) {// node的左节点和右节点都不为空时

                TreeNode<K,V> s = pr, sl;// s节点赋值为node的右节点

                //向左一直查找,直到叶子节点,跳出循环时,s为叶子节点
                while ((sl = s.left) != null) // find successor
                    s = sl;

                // 此文下面的所有操作都是为了实现将p节点和s节点进行位置调换，因此此处先将颜色替换
                boolean c = s.red; s.red = p.red; p.red = c; //交换p节点和s节点(叶子节点)的颜色

                // s的右节点
                TreeNode<K,V> sr = s.right;
                TreeNode<K,V> pp = p.parent;// p的父节点

                // 下面的第一次调整和第二次调整是"将p节点和s节点进行了位置调换"，
                // 然后找出要替换掉p节点的replacement；第三次调整是将replacement节点覆盖掉p节点；


                // 第一次调整start
                //
                // 如果p节点的右节点即为叶子节点，将p的父节点赋值为s，将s的右节点赋值为p即可；
                //    p        s
                //      s          p
                //
                if (s == pr) { // 如果p节点的右节点即为叶子节点// p was s's direct parent
                    p.parent = s;// 将p的父节点赋值为s
                    s.right = p;// 将s的右节点赋值为p
                }
                else {
                    // 否则，将p的父节点赋值为s的父节点sp，
                    // 并判断sp是否为空，如果不为空，并判断s是sp的左节点还是右节点，
                    // 将s节点替换为p节点；将s的右节点赋值为p节点的右节点pr，如果pr不为空则将pr的父节赋值为s节点。

                    TreeNode<K,V> sp = s.parent; // 将p的父节点赋值为s的父节点, 如果sp不为空
                    if ((p.parent = sp) != null) {
                        // 如果s节点为左节点
                        if (s == sp.left)
                            sp.left = p;// 则将s的父节点的左节点赋值为p节点
                        else// 如果s节点为右节点
                            sp.right = p;// 则将s的父节点的右节点赋值为p节点
                    }
                    if ((s.right = pr) != null)// s的右节点赋值为p节点的右节点
                        pr.parent = s;// p节点的右节点的父节点赋值为s
                }


                // 进行第二次调整：将p节点的左节点清空（上文pl已经保存了该节点）；
                // 将p节点的右节点赋值为s的右节点sr，如果sr不为空，则将sr的父节点赋值为p节点；
                // 将s节点的左节点赋值为p的左节点pl，如果pl不为空，则将p左节点的父节点赋值为s节点；
                // 将s的父节点赋值为p的父节点pp，如果pp为空，则p节点为root节点，此时交换后s成为新的root节点，
                // 将root赋值为s节点；如果p不为root节点，并且p是父节点的左节点，将p父节点的左节点赋值为s节点；
                // 如果p不为root节点，并且p是父节点的右节点，将p父节点的右节点赋值为s节点；如果sr不为空，
                // 将replacement赋值为sr节点，否则赋值为p节点（为什么sr是replacement的首选，p为备选？见解释1）



                // 第二次调整start

                p.left = null;
                if ((p.right = sr) != null)// 将p节点的右节点赋值为s的右节点, 如果sr不为空
                    sr.parent = p;// 则将s右节点的父节点赋值为p节点

                if ((s.left = pl) != null)// 将s节点的左节点赋值为p的左节点, 如果pl不为空
                    pl.parent = s;// 则将p左节点的父节点赋值为s节点

                if ((s.parent = pp) == null)// 将s的父节点赋值为p的父节点pp, 如果pp为空
                    root = s;// 则p节点为root节点, 此时交换后s成为新的root节点

                else if (p == pp.left)// 如果p不为root节点, 并且p是父节点的左节点
                    pp.left = s;// 将p父节点的左节点赋值为s节点

                else // 如果p不为root节点, 并且p是父节点的右节点
                    pp.right = s;// 将p父节点的右节点赋值为s节点


                if (sr != null)
                    replacement = sr;// 寻找replacement节点(用来替换掉p节点)
                else
                    replacement = p;// 寻找replacement节点
            }




            // 如果p的左节点不为空，右节点为空，将replacement赋值为p的左节点即可；如果p的右节点不为空，左节点为空
            // 将replacement赋值为p的右节点即可；如果p的左右节点都为空，即p为叶子节点, 将replacement赋值为p节点本身。

            else if (pl != null)// 如果p的左节点不为空,右节点为空,replacement节点为p的左节点
                replacement = pl;
            else if (pr != null)// 如果p的右节点不为空,左节点为空,replacement节点为p的右节点
                replacement = pr;
            else
                replacement = p;// 如果p的左右节点都为空, 即p为叶子节点, 替换节点为p节点本身


            // 进行第三次调整：如果p节点不是replacement（即p不是叶子节点），
            // 将replacement的父节点赋值为p的父节点，同事赋值给pp节点；
            // 如果pp为空（p节点没有父节点），即p为root节点，则将root节点赋值为replacement节点即可；
            // 如果p节点不是root节点，并且p节点为父节点的左节点，则将p父节点的左节点赋值为replacement节点；
            // 如果p节点不是root节点，并且p节点为父节点的右节点，则将p父节点的右节点赋值为replacement节点；
            // p节点的位置已经被完整的替换为replacement节点, 将p节点清空。




            // 第三次调整start
            if (replacement != p) {// 如果p节点不是叶子节点
                //将replacement节点的父节点赋值为p节点的父节点, 同时赋值给pp节点
                TreeNode<K,V> pp = replacement.parent = p.parent;
                if (pp == null)// 如果p节点没有父节点, 即p为root节点
                    root = replacement;// 则将root节点赋值为replacement节点即可
                else if (p == pp.left)// 如果p节点不是root节点, 并且p节点为父节点的左节点
                    pp.left = replacement; // 则将p父节点的左节点赋值为替换节点
                else // 如果p节点不是root节点, 并且p节点为父节点的右节点
                    pp.right = replacement;// 则将p父节点的右节点赋值为替换节点
                // p节点的位置已经被完整的替换为替换节点, 将p节点清空, 以便垃圾收集器回收
                p.left = p.right = p.parent = null;
            }

            // 如果p节点不为红色则进行红黑树删除平衡调整
            // (如果删除的节点是红色则不会破坏红黑树的平衡无需调整)
            TreeNode<K,V> r = p.red ? root : balanceDeletion(root, replacement);

            //如果p节点为叶子节点，则简单的将p节点移除：将pp赋值为p节点的父节点，将p的parent节点设置为空，如果p的父节点pp存在，
            // 如果p节点为父节点的左节点，则将父节点的左节点赋值为空，如果p节点为父节点的右节点，则将父节点的右节点赋值为空。
            if (replacement == p) { // 如果p节点为叶子节点, 则简单的将p节点去除即可
                TreeNode<K,V> pp = p.parent;// pp赋值为p节点的父节点
                p.parent = null;// 将p的parent节点设置为空
                if (pp != null) {// 如果p的父节点存在
                    if (p == pp.left)// 如果p节点为父节点的左节点
                        pp.left = null;// 则将父节点的左节点赋值为空
                    else if (p == pp.right)// 如果p节点为父节点的右节点
                        pp.right = null;// 则将父节点的右节点赋值为空
                }
            }

            // 如果movable为true，则调用moveRootToFront方法（见上文代码块8）将root节点移到索引位置的头结点。
            if (movable)
                moveRootToFront(tab, r);// 将root节点移到索引位置的头结点
        }

        /**
         * Splits nodes in a tree bin into lower and upper tree bins,
         * or untreeifies if now too small. Called only from resize;
         * see above discussion about split bits and indices.
         *
         * @param map the map
         * @param tab the table for recording bin heads
         * @param index the index of the table being split
         * @param bit the bit of hash to split on
         */
        //
        final void split(HashMap<K,V> map, Node<K,V>[] tab, int index, int bit) {
            // 拿到调用此方法的节点
            TreeNode<K,V> b = this;
            // 存储跟原索引位置相同的节点
            TreeNode<K,V> loHead = null, loTail = null;
            // 存储索引位置为:原索引+oldCap的节点
            TreeNode<K,V> hiHead = null, hiTail = null;


            int lc = 0, hc = 0;

            // 从b节点开始遍历
            for (TreeNode<K,V> e = b, next; e != null; e = next) {
                // next赋值为e的下个节点
                next = (TreeNode<K,V>)e.next;
                // 同时将老表的节点设置为空，以便垃圾收集器回收
                e.next = null;
                // 如果e的hash值与老表的容量进行与运算为0,则扩容后的索引位置跟老表的索引位置一样
                if ((e.hash & bit) == 0) {
                    // 如果loTail为空, 代表该节点为第一个节点
                    if ((e.prev = loTail) == null)
                        loHead = e;// 则将loHead赋值为第一个节点
                    else
                        loTail.next = e;// 否则将节点添加在loTail后面
                    loTail = e;// 并将loTail赋值为新增的节点
                    ++lc;// 统计原索引位置的节点个数
                }
                //如果e的hash值与老表的容量进行与运算为1,则扩容后的索引位置为:老表的索引位置＋oldCap
                else {
                    // 如果loTail为空, 代表该节点为第一个节点
                    if ((e.prev = hiTail) == null)// 如果hiHead为空, 代表该节点为第一个节点
                        hiHead = e;// 则将hiHead赋值为第一个节点
                    else
                        hiTail.next = e; // 否则将节点添加在hiTail后面
                    hiTail = e;// 并将hiTail赋值为新增的节点
                    ++hc;// 统计索引位置为原索引+oldCap的节点个数
                }
            }
            // 原索引位置的节点不为空;
            if (loHead != null) {
                // 节点个数少于6个则将红黑树转为链表结构
                // 如果当该索引位置节点数<=6个，调用untreeify方法（见下文代码块11）将红黑树节点转为链表节点；
                if (lc <= UNTREEIFY_THRESHOLD)
                    //
                    tab[index] = loHead.untreeify(map);
                else {
                    //否则将原索引位置的节点设置为对应的头结点（即loHead结点）
                    tab[index] = loHead;
                    // 如果判断hiHead不为空则代表原来的红黑树（老表的红黑树由于节点被分到两个位置）已经被改变，
                    // 需要重新构建新的红黑树，以loHead为根结点，调用treeify方法（见上文代码块7）构建新的红黑树
                    if (hiHead != null) // (else is already treeified)
                        loHead.treeify(tab);
                }
            }
            if (hiHead != null) {
                if (hc <= UNTREEIFY_THRESHOLD)
                    tab[index + bit] = hiHead.untreeify(map);
                else {
                    tab[index + bit] = hiHead;
                    if (loHead != null)
                        //如果判断loHead不为空则代表原来的红黑树（老表的红黑树由于节点被分到两个位置）已经被改变，
                        // 需要重新构建新的红黑树，以hiHead为根结点，调用treeify方法（见上文代码块7）构建新的红黑树
                        hiHead.treeify(tab);
                }
            }
        }

        /* ------------------------------------------------------------ */
        // Red-black tree methods, all adapted from CLR

        static <K,V> TreeNode<K,V> rotateLeft(TreeNode<K,V> root,
                                              TreeNode<K,V> p) {
            TreeNode<K,V> r, pp, rl;
            if (p != null && (r = p.right) != null) {
                if ((rl = p.right = r.left) != null)
                    rl.parent = p;
                if ((pp = r.parent = p.parent) == null)
                    (root = r).red = false;
                else if (pp.left == p)
                    pp.left = r;
                else
                    pp.right = r;
                r.left = p;
                p.parent = r;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> rotateRight(TreeNode<K,V> root,
                                               TreeNode<K,V> p) {
            TreeNode<K,V> l, pp, lr;
            if (p != null && (l = p.left) != null) {
                if ((lr = p.left = l.right) != null)
                    lr.parent = p;
                if ((pp = l.parent = p.parent) == null)
                    (root = l).red = false;
                else if (pp.right == p)
                    pp.right = l;
                else
                    pp.left = l;
                l.right = p;
                p.parent = l;
            }
            return root;
        }

        static <K,V> TreeNode<K,V> balanceInsertion(TreeNode<K,V> root,
                                                    TreeNode<K,V> x) {
            x.red = true;
            for (TreeNode<K,V> xp, xpp, xppl, xppr;;) {
                if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (!xp.red || (xpp = xp.parent) == null)
                    return root;
                if (xp == (xppl = xpp.left)) {
                    if ((xppr = xpp.right) != null && xppr.red) {
                        xppr.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.right) {
                            root = rotateLeft(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateRight(root, xpp);
                            }
                        }
                    }
                }
                else {
                    if (xppl != null && xppl.red) {
                        xppl.red = false;
                        xp.red = false;
                        xpp.red = true;
                        x = xpp;
                    }
                    else {
                        if (x == xp.left) {
                            root = rotateRight(root, x = xp);
                            xpp = (xp = x.parent) == null ? null : xp.parent;
                        }
                        if (xp != null) {
                            xp.red = false;
                            if (xpp != null) {
                                xpp.red = true;
                                root = rotateLeft(root, xpp);
                            }
                        }
                    }
                }
            }
        }

        static <K,V> TreeNode<K,V> balanceDeletion(TreeNode<K,V> root,
                                                   TreeNode<K,V> x) {
            for (TreeNode<K,V> xp, xpl, xpr;;)  {
                if (x == null || x == root)
                    return root;
                else if ((xp = x.parent) == null) {
                    x.red = false;
                    return x;
                }
                else if (x.red) {
                    x.red = false;
                    return root;
                }
                else if ((xpl = xp.left) == x) {
                    if ((xpr = xp.right) != null && xpr.red) {
                        xpr.red = false;
                        xp.red = true;
                        root = rotateLeft(root, xp);
                        xpr = (xp = x.parent) == null ? null : xp.right;
                    }
                    if (xpr == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpr.left, sr = xpr.right;
                        if ((sr == null || !sr.red) &&
                            (sl == null || !sl.red)) {
                            xpr.red = true;
                            x = xp;
                        }
                        else {
                            if (sr == null || !sr.red) {
                                if (sl != null)
                                    sl.red = false;
                                xpr.red = true;
                                root = rotateRight(root, xpr);
                                xpr = (xp = x.parent) == null ?
                                    null : xp.right;
                            }
                            if (xpr != null) {
                                xpr.red = (xp == null) ? false : xp.red;
                                if ((sr = xpr.right) != null)
                                    sr.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateLeft(root, xp);
                            }
                            x = root;
                        }
                    }
                }
                else { // symmetric
                    if (xpl != null && xpl.red) {
                        xpl.red = false;
                        xp.red = true;
                        root = rotateRight(root, xp);
                        xpl = (xp = x.parent) == null ? null : xp.left;
                    }
                    if (xpl == null)
                        x = xp;
                    else {
                        TreeNode<K,V> sl = xpl.left, sr = xpl.right;
                        if ((sl == null || !sl.red) &&
                            (sr == null || !sr.red)) {
                            xpl.red = true;
                            x = xp;
                        }
                        else {
                            if (sl == null || !sl.red) {
                                if (sr != null)
                                    sr.red = false;
                                xpl.red = true;
                                root = rotateLeft(root, xpl);
                                xpl = (xp = x.parent) == null ?
                                    null : xp.left;
                            }
                            if (xpl != null) {
                                xpl.red = (xp == null) ? false : xp.red;
                                if ((sl = xpl.left) != null)
                                    sl.red = false;
                            }
                            if (xp != null) {
                                xp.red = false;
                                root = rotateRight(root, xp);
                            }
                            x = root;
                        }
                    }
                }
            }
        }

        /**
         * Recursive invariant check
         */
        // 一些基本的校验
        // 将传入的节点作为根结点，遍历所有节点，校验节点的合法性，主要是保证该树符合红黑树的规则。
        static <K,V> boolean checkInvariants(TreeNode<K,V> t) {

            //
            TreeNode<K,V> tp = t.parent, tl = t.left, tr = t.right,

                tb = t.prev, tn = (TreeNode<K,V>)t.next;
            //
            if (tb != null && tb.next != t)
                return false;
            //
            if (tn != null && tn.prev != t)
                return false;
            //
            if (tp != null && t != tp.left && t != tp.right)
                return false;

            if (tl != null && (tl.parent != t || tl.hash > t.hash))
                return false;

            if (tr != null && (tr.parent != t || tr.hash < t.hash))
                return false;

            if (t.red && tl != null && tl.red && tr != null && tr.red) // 如果当前节点为红色, 则该节点的左右节点都不能为红色
                return false;
            if (tl != null && !checkInvariants(tl))
                return false;
            if (tr != null && !checkInvariants(tr))
                return false;
            return true;
        }
    }

}
