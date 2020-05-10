/*
 * Copyright (c) 1997, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package java.util;

import java.io.Serializable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * A Red-Black tree based {@link NavigableMap} implementation.
 * The map is sorted according to the {@linkplain Comparable natural
 * ordering} of its keys, or by a {@link Comparator} provided at map
 * creation time, depending on which constructor is used.
 *
 * <p>This implementation provides guaranteed log(n) time cost for the
 * {@code containsKey}, {@code get}, {@code put} and {@code remove}
 * operations.  Algorithms are adaptations of those in Cormen, Leiserson, and
 * Rivest's <em>Introduction to Algorithms</em>.
 *
 * <p>Note that the ordering maintained by a tree map, like any sorted map, and
 * whether or not an explicit comparator is provided, must be <em>consistent
 * with {@code equals}</em> if this sorted map is to correctly implement the
 * {@code Map} interface.  (See {@code Comparable} or {@code Comparator} for a
 * precise definition of <em>consistent with equals</em>.)  This is so because
 * the {@code Map} interface is defined in terms of the {@code equals}
 * operation, but a sorted map performs all key comparisons using its {@code
 * compareTo} (or {@code compare}) method, so two keys that are deemed equal by
 * this method are, from the standpoint of the sorted map, equal.  The behavior
 * of a sorted map <em>is</em> well-defined even if its ordering is
 * inconsistent with {@code equals}; it just fails to obey the general contract
 * of the {@code Map} interface.
 *
 * <p><strong>Note that this implementation is not synchronized.</strong>
 * If multiple threads access a map concurrently, and at least one of the
 * threads modifies the map structurally, it <em>must</em> be synchronized
 * externally.  (A structural modification is any operation that adds or
 * deletes one or more mappings; merely changing the value associated
 * with an existing key is not a structural modification.)  This is
 * typically accomplished by synchronizing on some object that naturally
 * encapsulates the map.
 * If no such object exists, the map should be "wrapped" using the
 * {@link Collections#synchronizedSortedMap Collections.synchronizedSortedMap}
 * method.  This is best done at creation time, to prevent accidental
 * unsynchronized access to the map: <pre>
 *   SortedMap m = Collections.synchronizedSortedMap(new TreeMap(...));</pre>
 *
 * <p>The iterators returned by the {@code iterator} method of the collections
 * returned by all of this class's "collection view methods" are
 * <em>fail-fast</em>: if the map is structurally modified at any time after
 * the iterator is created, in any way except through the iterator's own
 * {@code remove} method, the iterator will throw a {@link
 * ConcurrentModificationException}.  Thus, in the face of concurrent
 * modification, the iterator fails quickly and cleanly, rather than risking
 * arbitrary, non-deterministic behavior at an undetermined time in the future.
 *
 * <p>Note that the fail-fast behavior of an iterator cannot be guaranteed
 * as it is, generally speaking, impossible to make any hard guarantees in the
 * presence of unsynchronized concurrent modification.  Fail-fast iterators
 * throw {@code ConcurrentModificationException} on a best-effort basis.
 * Therefore, it would be wrong to write a program that depended on this
 * exception for its correctness:   <em>the fail-fast behavior of iterators
 * should be used only to detect bugs.</em>
 *
 * <p>All {@code Map.Entry} pairs returned by methods in this class
 * and its views represent snapshots of mappings at the time they were
 * produced. They do <strong>not</strong> support the {@code Entry.setValue}
 * method. (Note however that it is possible to change mappings in the
 * associated map using {@code put}.)
 *
 * <p>This class is a member of the
 * <a href="{@docRoot}/java.base/java/util/package-summary.html#CollectionsFramework">
 * Java Collections Framework</a>.
 *
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 *
 * @author  Josh Bloch and Doug Lea
 * @see Map
 * @see HashMap
 * @see Hashtable
 * @see Comparable
 * @see Comparator
 * @see Collection
 * @since 1.2
 */

public class TreeMap<K,V>
    extends AbstractMap<K,V>
    implements NavigableMap<K,V>, Cloneable, java.io.Serializable
{
    /**
     * key 排序器
     */
    /**
     * The comparator used to maintain order in this tree map, or
     * null if it uses the natural ordering of its keys.
     *
     * @serial
     */
    @SuppressWarnings("serial") // Conditionally serializable
    private final Comparator<? super K> comparator;

    /**
     * 红黑树的根节点
     */
    private transient Entry<K,V> root;

    /**
     * key-value 键值对数量
     */
    /**
     * The number of entries in the tree
     */
    private transient int size = 0;

    /**
     * 修改次数
     */
    /**
     * The number of structural modifications to the tree.
     */
    private transient int modCount = 0;

    /**
     * Constructs a new, empty tree map, using the natural ordering of its
     * keys.  All keys inserted into the map must implement the {@link
     * Comparable} interface.  Furthermore, all such keys must be
     * <em>mutually comparable</em>: {@code k1.compareTo(k2)} must not throw
     * a {@code ClassCastException} for any keys {@code k1} and
     * {@code k2} in the map.  If the user attempts to put a key into the
     * map that violates this constraint (for example, the user attempts to
     * put a string key into a map whose keys are integers), the
     * {@code put(Object key, Object value)} call will throw a
     * {@code ClassCastException}.
     */
    public TreeMap() {
        comparator = null;
    }

    /**
     * Constructs a new, empty tree map, ordered according to the given
     * comparator.  All keys inserted into the map must be <em>mutually
     * comparable</em> by the given comparator: {@code comparator.compare(k1,
     * k2)} must not throw a {@code ClassCastException} for any keys
     * {@code k1} and {@code k2} in the map.  If the user attempts to put
     * a key into the map that violates this constraint, the {@code put(Object
     * key, Object value)} call will throw a
     * {@code ClassCastException}.
     *
     * @param comparator the comparator that will be used to order this map.
     *        If {@code null}, the {@linkplain Comparable natural
     *        ordering} of the keys will be used.
     */
    public TreeMap(Comparator<? super K> comparator) {
        // 可传入 comparator 参数，自定义 key 的排序规则
        this.comparator = comparator;
    }

    /**
     * Constructs a new tree map containing the same mappings as the given
     * map, ordered according to the <em>natural ordering</em> of its keys.
     * All keys inserted into the new map must implement the {@link
     * Comparable} interface.  Furthermore, all such keys must be
     * <em>mutually comparable</em>: {@code k1.compareTo(k2)} must not throw
     * a {@code ClassCastException} for any keys {@code k1} and
     * {@code k2} in the map.  This method runs in n*log(n) time.
     *
     * @param  m the map whose mappings are to be placed in this map
     * @throws ClassCastException if the keys in m are not {@link Comparable},
     *         or are not mutually comparable
     * @throws NullPointerException if the specified map is null
     */
    public TreeMap(Map<? extends K, ? extends V> m) {
        comparator = null;
        // 添加所有元素
        putAll(m);
    }

    /**
     * Constructs a new tree map containing the same mappings and
     * using the same ordering as the specified sorted map.  This
     * method runs in linear time.
     *
     * @param  m the sorted map whose mappings are to be placed in this map,
     *         and whose comparator is to be used to sort this map
     * @throws NullPointerException if the specified map is null
     */
    public TreeMap(SortedMap<K, ? extends V> m) {
        // <1> 设置 comparator 属性
        comparator = m.comparator();
        try {
            // <2> 使用 m 构造红黑树
            buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
        } catch (java.io.IOException | ClassNotFoundException cannotHappen) {
        }
    }


    // Query Operations

    /**
     * Returns the number of key-value mappings in this map.
     *
     * @return the number of key-value mappings in this map
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this map contains a mapping for the specified
     * key.
     *
     * @param key key whose presence in this map is to be tested
     * @return {@code true} if this map contains a mapping for the
     *         specified key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     */
    public boolean containsKey(Object key) {
        return getEntry(key) != null;
    }

    /**
     * Returns {@code true} if this map maps one or more keys to the
     * specified value.  More formally, returns {@code true} if and only if
     * this map contains at least one mapping to a value {@code v} such
     * that {@code (value==null ? v==null : value.equals(v))}.  This
     * operation will probably require time linear in the map size for
     * most implementations.
     *
     * @param value value whose presence in this map is to be tested
     * @return {@code true} if a mapping to {@code value} exists;
     *         {@code false} otherwise
     * @since 1.2
     */
    public boolean containsValue(Object value) {
        for (Entry<K,V> e = getFirstEntry();   // 获得首个 Entry 节点
             e != null;   // 遍历到没有下一个节点
             e = successor(e))  // 通过中序遍历，获得下一个节点
            if (valEquals(value, e.value))   // 判断值是否相等
                return true;
        return false;
    }

    /**
     * Returns the value to which the specified key is mapped,
     * or {@code null} if this map contains no mapping for the key.
     *
     * <p>More formally, if this map contains a mapping from a key
     * {@code k} to a value {@code v} such that {@code key} compares
     * equal to {@code k} according to the map's ordering, then this
     * method returns {@code v}; otherwise it returns {@code null}.
     * (There can be at most one such mapping.)
     *
     * <p>A return value of {@code null} does not <em>necessarily</em>
     * indicate that the map contains no mapping for the key; it's also
     * possible that the map explicitly maps the key to {@code null}.
     * The {@link #containsKey containsKey} operation may be used to
     * distinguish these two cases.
     *
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     */
    public V get(Object key) {
        // 获得 key 对应的 Entry 节点
        Entry<K,V> p = getEntry(key);
        // 返回 value 值
        return (p==null ? null : p.value);
    }

    public Comparator<? super K> comparator() {
        return comparator;
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public K firstKey() {
        return key(getFirstEntry());
    }

    /**
     * @throws NoSuchElementException {@inheritDoc}
     */
    public K lastKey() {
        return key(getLastEntry());
    }

    /**
     * Copies all of the mappings from the specified map to this map.
     * These mappings replace any mappings that this map had for any
     * of the keys currently in the specified map.
     *
     * @param  map mappings to be stored in this map
     * @throws ClassCastException if the class of a key or value in
     *         the specified map prevents it from being stored in this map
     * @throws NullPointerException if the specified map is null or
     *         the specified map contains a null key and this map does not
     *         permit null keys
     */
    public void putAll(Map<? extends K, ? extends V> map) {
        // <1> 路径一，满足如下条件，调用 buildFromSorted 方法来优化处理
        int mapSize = map.size();
        if (size==0   // 如果 TreeMap 的大小为 0
                && mapSize!=0    // map 的大小非 0
                && map instanceof SortedMap) {  // 如果是 map 是 SortedMap 类型
            if (Objects.equals(comparator, ((SortedMap<?,?>)map).comparator())) {  // 排序规则相同
                // 增加修改次数
                ++modCount;
                // 基于 SortedMap 顺序迭代插入即可
                try {
                    buildFromSorted(mapSize, map.entrySet().iterator(),
                                    null, null);
                } catch (java.io.IOException | ClassNotFoundException cannotHappen) {
                }
                return;
            }
        }
        // <2> 路径二，直接遍历 map 来添加
        super.putAll(map);
    }

    /**
     * Returns this map's entry for the given key, or {@code null} if the map
     * does not contain an entry for the key.
     *
     * @return this map's entry for the given key, or {@code null} if the map
     *         does not contain an entry for the key
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     */
    final Entry<K,V> getEntry(Object key) {  // 不使用 comparator 查找
        // Offload comparator-based version for sake of performance
        // 如果自定义了 comparator 比较器，则基于 comparator 比较来查找
        if (comparator != null)
            return getEntryUsingComparator(key);
        // 如果 key 为空，抛出异常
        if (key == null)
            throw new NullPointerException();
        @SuppressWarnings("unchecked")
            Comparable<? super K> k = (Comparable<? super K>) key;
        // 遍历红黑树
        Entry<K,V> p = root;
        while (p != null) {
            // 比较值
            int cmp = k.compareTo(p.key);
            // 如果 key 小于当前节点，则遍历左子树
            if (cmp < 0)
                p = p.left;
                // 如果 key 大于当前节点，则遍历右子树
            else if (cmp > 0)
                p = p.right;
                // 如果 key 相等，则返回该节点
            else
                // 查找不到，返回 null
                return p;
        }
        return null;
    }

    /**
     * Version of getEntry using comparator. Split off from getEntry
     * for performance. (This is not worth doing for most methods,
     * that are less dependent on comparator performance, but is
     * worthwhile here.)
     */
    final Entry<K,V> getEntryUsingComparator(Object key) {   // 使用 comparator 查找
        @SuppressWarnings("unchecked")
            K k = (K) key;
        Comparator<? super K> cpr = comparator;
        if (cpr != null) {
            // 遍历红黑树
            Entry<K,V> p = root;
            while (p != null) {
                // 比较值
                int cmp = cpr.compare(k, p.key);
                // 如果 key 小于当前节点，则遍历左子树
                if (cmp < 0)
                    p = p.left;
                    // 如果 key 大于当前节点，则遍历右子树
                else if (cmp > 0)
                    p = p.right;
                    // 如果 key 相等，则返回该节点
                else
                    return p;
            }
        }
        // 查找不到，返回 null
        return null;
    }

    /**
     * Gets the entry corresponding to the specified key; if no such entry
     * exists, returns the entry for the least key greater than the specified
     * key; if no such entry exists (i.e., the greatest key in the Tree is less
     * than the specified key), returns {@code null}.
     */
    final Entry<K,V> getCeilingEntry(K key) {    // 大于等于 key 的节点
        Entry<K,V> p = root;
        // <3> 循环二叉查找遍历红黑树
        while (p != null) {
            // <3.1> 比较 key
            int cmp = compare(key, p.key);
            // <3.2> 当前节点比 key 大，则遍历左子树，这样缩小节点的值
            if (cmp < 0) {
                // <3.2.1> 如果有左子树，则遍历左子树
                if (p.left != null)
                    p = p.left;
                    // <3.2.2.> 如果没有，则直接返回该节点
                else
                    return p;
                // <3.3> 当前节点比 key 小，则遍历右子树，这样放大节点的值
            } else if (cmp > 0) {
                // <3.3.1> 如果有右子树，则遍历右子树
                if (p.right != null) {
                    p = p.right;
                } else {
                    // <3.3.2> 找到当前的后继节点
                    Entry<K,V> parent = p.parent;
                    Entry<K,V> ch = p;
                    while (parent != null && ch == parent.right) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
                // <3.4> 如果相等，则返回该节点即可
            } else
                return p;
        }
        // <3.5>
        return null;
    }

    /**
     * Gets the entry corresponding to the specified key; if no such entry
     * exists, returns the entry for the greatest key less than the specified
     * key; if no such entry exists, returns {@code null}.
     */
    final Entry<K,V> getFloorEntry(K key) {   // 小于等于 key 的节点
        Entry<K,V> p = root;
        // 循环二叉查找遍历红黑树
        while (p != null) {
            // 比较 key
            int cmp = compare(key, p.key);
            if (cmp > 0) {
                // 如果有右子树，则遍历右子树
                if (p.right != null)
                    p = p.right;
                    // 如果没有，则直接返回该节点
                else
                    return p;
                // 当前节点比 key 大，则遍历左子树，这样缩小节点的值
            } else if (cmp < 0) {
                // 如果有左子树，则遍历左子树
                if (p.left != null) {
                    p = p.left;
                } else {
                    // 找到当前节点的前继节点
                    Entry<K,V> parent = p.parent;
                    Entry<K,V> ch = p;
                    while (parent != null && ch == parent.left) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
                // 如果相等，则返回该节点即可
            } else
                return p;

        }
        // 查找不到，返回 null
        return null;
    }

    /**
     * Gets the entry for the least key greater than the specified
     * key; if no such entry exists, returns the entry for the least
     * key greater than the specified key; if no such entry exists
     * returns {@code null}.
     */
    final Entry<K,V> getHigherEntry(K key) {   // 大于 key 的节点
        Entry<K,V> p = root;
        // 循环二叉查找遍历红黑树
        while (p != null) {
            // 比较 key
            int cmp = compare(key, p.key);
            // 当前节点比 key 大，则遍历左子树，这样缩小节点的值
            if (cmp < 0) {
                // 如果有左子树，则遍历左子树
                if (p.left != null)
                    p = p.left;
                    // 如果没有，则直接返回该节点
                else
                    return p;
                // 当前节点比 key 小，则遍历右子树，这样放大节点的值
            } else {
                // 如果有右子树，则遍历右子树
                if (p.right != null) {
                    p = p.right;
                } else {
                    // 找到当前的后继节点
                    Entry<K,V> parent = p.parent;
                    Entry<K,V> ch = p;
                    while (parent != null && ch == parent.right) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            }
            // <X> 此处，相等的情况下，不返回
        }
        // 查找不到，返回 null
        return null;
    }

    /**
     * Returns the entry for the greatest key less than the specified key; if
     * no such entry exists (i.e., the least key in the Tree is greater than
     * the specified key), returns {@code null}.
     */
    final Entry<K,V> getLowerEntry(K key) {
        Entry<K,V> p = root;
        // 循环二叉查找遍历红黑树
        while (p != null) {
            // 比较 key
            int cmp = compare(key, p.key);
            // 当前节点比 key 小，则遍历右子树，这样放大节点的值
            if (cmp > 0) {
                // 如果有右子树，则遍历右子树
                if (p.right != null)
                    p = p.right;
                    // 如果没有，则直接返回该节点
                else
                    return p;
                // 当前节点比 key 大，则遍历左子树，这样缩小节点的值
            } else {
                // 如果有左子树，则遍历左子树
                if (p.left != null) {
                    p = p.left;
                } else {
                    // 找到当前节点的前继节点
                    Entry<K,V> parent = p.parent;
                    Entry<K,V> ch = p;
                    while (parent != null && ch == parent.left) {
                        ch = parent;
                        parent = parent.parent;
                    }
                    return parent;
                }
            }
            // 此处，相等的情况下，不返回
        }
        // 查找不到，返回 null
        return null;
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old
     * value is replaced.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     *
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with {@code key}.)
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     */
    public V put(K key, V value) {
        // 记录当前根节点
        Entry<K,V> t = root;
        // <1> 如果无根节点，则直接使用 key-value 键值对，创建根节点
        if (t == null) {
            // <1.1> 校验 key 类型。
            compare(key, key); // type (and possibly null) check

            // <1.2> 创建 Entry 节点
            root = new Entry<>(key, value, null);
            // <1.3> 设置 key-value 键值对的数量
            size = 1;
            // <1.4> 增加修改次数
            modCount++;
            return null;
        }
        // <2> 遍历红黑树
        int cmp;  // key 比父节点小还是大
        Entry<K,V> parent;  // 父节点
        // split comparator and comparable paths
        Comparator<? super K> cpr = comparator;
        if (cpr != null) {   // 如果有自定义 comparator ，则使用它来比较
            do {
                // <2.1> 记录新的父节点
                parent = t;
                // <2.2> 比较 key
                cmp = cpr.compare(key, t.key);
                // <2.3> 比 key 小，说明要遍历左子树
                if (cmp < 0)
                    t = t.left;
                    // <2.4> 比 key 大，说明要遍历右子树
                else if (cmp > 0)
                    t = t.right;
                    // <2.5> 说明，相等，说明要找到的 t 就是 key 对应的节点，直接设置 value 即可。
                else
                    return t.setValue(value);
            } while (t != null);   // <2.6>
        }
        else {  // 如果没有自定义 comparator ，则使用 key 自身比较器来比较
            if (key == null)   // 如果 key 为空，则抛出异常
                throw new NullPointerException();
            @SuppressWarnings("unchecked")
                Comparable<? super K> k = (Comparable<? super K>) key;
            do {
                // <2.1> 记录新的父节点
                parent = t;
                // <2.2> 比较 key
                cmp = k.compareTo(t.key);
                // <2.3> 比 key 小，说明要遍历左子树
                if (cmp < 0)
                    t = t.left;
                    // <2.4> 比 key 大，说明要遍历右子树
                else if (cmp > 0)
                    t = t.right;
                    // <2.5> 说明，相等，说明要找到的 t 就是 key 对应的节点，直接设置 value 即可。
                else
                    return t.setValue(value);
            } while (t != null);
        }
        // <3> 创建 key-value 的 Entry 节点
        Entry<K,V> e = new Entry<>(key, value, parent);
        // 设置左右子树
        if (cmp < 0)   // <3.1>
            parent.left = e;
        else  // <3.2>
            parent.right = e;
        // <3.3> 插入后，进行自平衡
        fixAfterInsertion(e);
        // <3.4> 设置 key-value 键值对的数量
        size++;
        // <3.5> 增加修改次数
        modCount++;
        return null;
    }

    /**
     * Removes the mapping for this key from this TreeMap if present.
     *
     * @param  key key for which mapping should be removed
     * @return the previous value associated with {@code key}, or
     *         {@code null} if there was no mapping for {@code key}.
     *         (A {@code null} return can also indicate that the map
     *         previously associated {@code null} with {@code key}.)
     * @throws ClassCastException if the specified key cannot be compared
     *         with the keys currently in the map
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     */
    public V remove(Object key) {
        // <1> 获得 key 对应的 Entry 节点
        Entry<K,V> p = getEntry(key);
        // <2> 如果不存在，则返回 null ，无需删除
        if (p == null)
            return null;

        V oldValue = p.value;
        // <3> 删除节点
        deleteEntry(p);
        return oldValue;
    }

    /**
     * Removes all of the mappings from this map.
     * The map will be empty after this call returns.
     */
    public void clear() {
        // 增加修改次数
        modCount++;
        // key-value 数量置为 0
        size = 0;
        // 设置根节点为 null
        root = null;
    }

    /**
     * Returns a shallow copy of this {@code TreeMap} instance. (The keys and
     * values themselves are not cloned.)
     *
     * @return a shallow copy of this map
     */
    public Object clone() {
        // 克隆创建 TreeMap 对象
        TreeMap<?,?> clone;
        try {
            clone = (TreeMap<?,?>) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }

        // Put clone into "virgin" state (except for comparator)
        // 重置 clone 对象的属性
        clone.root = null;
        clone.size = 0;
        clone.modCount = 0;
        clone.entrySet = null;
        clone.navigableKeySet = null;
        clone.descendingMap = null;

        // Initialize clone with our mappings
        try {
            // 使用自己，构造 clone 对象的红黑树
            clone.buildFromSorted(size, entrySet().iterator(), null, null);
        } catch (java.io.IOException | ClassNotFoundException cannotHappen) {
        }

        return clone;
    }

    // NavigableMap API methods

    /**
     * @since 1.6
     */
    public Map.Entry<K,V> firstEntry() {   // 获得首个 Entry 节点
        return exportEntry(getFirstEntry());
    }

    /**
     * @since 1.6
     */
    public Map.Entry<K,V> lastEntry() {   // 获得末尾 Entry 节点
        return exportEntry(getLastEntry());
    }

    /**
     * @since 1.6
     */
    public Map.Entry<K,V> pollFirstEntry() {    // 获得并移除首个 Entry 节点
        // 获得首个 Entry 节点
        Entry<K,V> p = getFirstEntry();
        Map.Entry<K,V> result = exportEntry(p);
        // 如果存在，则进行删除
        if (p != null)
            deleteEntry(p);
        return result;
    }

    /**
     * @since 1.6
     */
    public Map.Entry<K,V> pollLastEntry() {  // 获得并移除尾部 Entry 节点
        // 获得尾部 Entry 节点
        Entry<K,V> p = getLastEntry();
        Map.Entry<K,V> result = exportEntry(p);
        // 如果存在，则进行删除。
        if (p != null)
            deleteEntry(p);
        return result;
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @since 1.6
     */
    public Map.Entry<K,V> lowerEntry(K key) {
        return exportEntry(getLowerEntry(key));
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @since 1.6
     */
    public K lowerKey(K key) {
        return keyOrNull(getLowerEntry(key));
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @since 1.6
     */
    public Map.Entry<K,V> floorEntry(K key) {
        return exportEntry(getFloorEntry(key));
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @since 1.6
     */
    public K floorKey(K key) {
        return keyOrNull(getFloorEntry(key));
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @since 1.6
     */
    public Map.Entry<K,V> ceilingEntry(K key) {
        return exportEntry(getCeilingEntry(key));
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @since 1.6
     */
    public K ceilingKey(K key) {
        return keyOrNull(getCeilingEntry(key));
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @since 1.6
     */
    public Map.Entry<K,V> higherEntry(K key) {
        return exportEntry(getHigherEntry(key));
    }

    /**
     * @throws ClassCastException {@inheritDoc}
     * @throws NullPointerException if the specified key is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @since 1.6
     */
    public K higherKey(K key) {
        return keyOrNull(getHigherEntry(key));
    }

    // Views

    /**
     * Entry 缓存集合
     */
    /**
     * Fields initialized to contain an instance of the entry set view
     * the first time this view is requested.  Views are stateless, so
     * there's no reason to create more than one.
     */
    private transient EntrySet entrySet;
    /**
     * 正序的 KeySet 缓存对象
     */
    private transient KeySet<K> navigableKeySet;
    /**
     * 倒序的 NavigableMap 缓存对象
     */
    private transient NavigableMap<K,V> descendingMap;

    /**
     * Returns a {@link Set} view of the keys contained in this map.
     *
     * <p>The set's iterator returns the keys in ascending order.
     * The set's spliterator is
     * <em><a href="Spliterator.html#binding">late-binding</a></em>,
     * <em>fail-fast</em>, and additionally reports {@link Spliterator#SORTED}
     * and {@link Spliterator#ORDERED} with an encounter order that is ascending
     * key order.  The spliterator's comparator (see
     * {@link java.util.Spliterator#getComparator()}) is {@code null} if
     * the tree map's comparator (see {@link #comparator()}) is {@code null}.
     * Otherwise, the spliterator's comparator is the same as or imposes the
     * same total ordering as the tree map's comparator.
     *
     * <p>The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own {@code remove} operation), the results of
     * the iteration are undefined.  The set supports element removal,
     * which removes the corresponding mapping from the map, via the
     * {@code Iterator.remove}, {@code Set.remove},
     * {@code removeAll}, {@code retainAll}, and {@code clear}
     * operations.  It does not support the {@code add} or {@code addAll}
     * operations.
     */
    public Set<K> keySet() {
        return navigableKeySet();
    }

    /**
     * @since 1.6
     */
    public NavigableSet<K> navigableKeySet() {
        KeySet<K> nks = navigableKeySet;
        return (nks != null) ? nks : (navigableKeySet = new KeySet<>(this));
    }

    /**
     * @since 1.6
     */
    public NavigableSet<K> descendingKeySet() {
        return descendingMap().navigableKeySet();
    }

    /**
     * Returns a {@link Collection} view of the values contained in this map.
     *
     * <p>The collection's iterator returns the values in ascending order
     * of the corresponding keys. The collection's spliterator is
     * <em><a href="Spliterator.html#binding">late-binding</a></em>,
     * <em>fail-fast</em>, and additionally reports {@link Spliterator#ORDERED}
     * with an encounter order that is ascending order of the corresponding
     * keys.
     *
     * <p>The collection is backed by the map, so changes to the map are
     * reflected in the collection, and vice-versa.  If the map is
     * modified while an iteration over the collection is in progress
     * (except through the iterator's own {@code remove} operation),
     * the results of the iteration are undefined.  The collection
     * supports element removal, which removes the corresponding
     * mapping from the map, via the {@code Iterator.remove},
     * {@code Collection.remove}, {@code removeAll},
     * {@code retainAll} and {@code clear} operations.  It does not
     * support the {@code add} or {@code addAll} operations.
     */
    public Collection<V> values() {
        Collection<V> vs = values;
        if (vs == null) {
            vs = new Values();
            values = vs;  // values 缓存，来自 AbstractMap 的属性
        }
        return vs;
    }

    /**
     * Returns a {@link Set} view of the mappings contained in this map.
     *
     * <p>The set's iterator returns the entries in ascending key order. The
     * set's spliterator is
     * <em><a href="Spliterator.html#binding">late-binding</a></em>,
     * <em>fail-fast</em>, and additionally reports {@link Spliterator#SORTED} and
     * {@link Spliterator#ORDERED} with an encounter order that is ascending key
     * order.
     *
     * <p>The set is backed by the map, so changes to the map are
     * reflected in the set, and vice-versa.  If the map is modified
     * while an iteration over the set is in progress (except through
     * the iterator's own {@code remove} operation, or through the
     * {@code setValue} operation on a map entry returned by the
     * iterator) the results of the iteration are undefined.  The set
     * supports element removal, which removes the corresponding
     * mapping from the map, via the {@code Iterator.remove},
     * {@code Set.remove}, {@code removeAll}, {@code retainAll} and
     * {@code clear} operations.  It does not support the
     * {@code add} or {@code addAll} operations.
     */
    public Set<Map.Entry<K,V>> entrySet() {
        EntrySet es = entrySet;
        return (es != null) ? es : (entrySet = new EntrySet());
    }

    /**
     * @since 1.6
     */
    public NavigableMap<K, V> descendingMap() {
        NavigableMap<K, V> km = descendingMap;
        return (km != null) ? km :
            (descendingMap = new DescendingSubMap<>(this,
                                                    true, null, true,
                                                    true, null, true));
    }

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException if {@code fromKey} or {@code toKey} is
     *         null and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @throws IllegalArgumentException {@inheritDoc}
     * @since 1.6
     */
    public NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
                                    K toKey,   boolean toInclusive) {
        return new AscendingSubMap<>(this,
                                     false, fromKey, fromInclusive,
                                     false, toKey,   toInclusive);
    }

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException if {@code toKey} is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @throws IllegalArgumentException {@inheritDoc}
     * @since 1.6
     */
    public NavigableMap<K,V> headMap(K toKey, boolean inclusive) {
        return new AscendingSubMap<>(this,
                                     true,  null,  true,
                                     false, toKey, inclusive);
    }

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException if {@code fromKey} is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @throws IllegalArgumentException {@inheritDoc}
     * @since 1.6
     */
    public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
        return new AscendingSubMap<>(this,
                                     false, fromKey, inclusive,
                                     true,  null,    true);
    }

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException if {@code fromKey} or {@code toKey} is
     *         null and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public SortedMap<K,V> subMap(K fromKey, K toKey) {
        return subMap(fromKey, true, toKey, false);
    }

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException if {@code toKey} is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public SortedMap<K,V> headMap(K toKey) {
        return headMap(toKey, false);
    }

    /**
     * @throws ClassCastException       {@inheritDoc}
     * @throws NullPointerException if {@code fromKey} is null
     *         and this map uses natural ordering, or its comparator
     *         does not permit null keys
     * @throws IllegalArgumentException {@inheritDoc}
     */
    public SortedMap<K,V> tailMap(K fromKey) {
        return tailMap(fromKey, true);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        Entry<K,V> p = getEntry(key);
        if (p!=null && Objects.equals(oldValue, p.value)) {
            p.value = newValue;
            return true;
        }
        return false;
    }

    @Override
    public V replace(K key, V value) {
        Entry<K,V> p = getEntry(key);
        if (p!=null) {
            V oldValue = p.value;
            p.value = value;
            return oldValue;
        }
        return null;
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        int expectedModCount = modCount;
        for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
            action.accept(e.key, e.value);

            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        int expectedModCount = modCount;

        for (Entry<K, V> e = getFirstEntry(); e != null; e = successor(e)) {
            e.value = function.apply(e.key, e.value);

            if (expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
        }
    }

    // View class support

    class Values extends AbstractCollection<V> {
        public Iterator<V> iterator() {
            return new ValueIterator(getFirstEntry());
        }

        public int size() {
            return TreeMap.this.size();
        }

        public boolean contains(Object o) {
            return TreeMap.this.containsValue(o);
        }

        public boolean remove(Object o) {
            for (Entry<K,V> e = getFirstEntry(); e != null; e = successor(e)) {
                if (valEquals(e.getValue(), o)) {
                    deleteEntry(e);
                    return true;
                }
            }
            return false;
        }

        public void clear() {
            TreeMap.this.clear();
        }

        public Spliterator<V> spliterator() {
            return new ValueSpliterator<>(TreeMap.this, null, null, 0, -1, 0);
        }
    }

    class EntrySet extends AbstractSet<Map.Entry<K,V>> {
        public Iterator<Map.Entry<K,V>> iterator() {
            return new EntryIterator(getFirstEntry());
        }

        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
            Object value = entry.getValue();
            Entry<K,V> p = getEntry(entry.getKey());
            return p != null && valEquals(p.getValue(), value);
        }

        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
            Object value = entry.getValue();
            Entry<K,V> p = getEntry(entry.getKey());
            if (p != null && valEquals(p.getValue(), value)) {
                deleteEntry(p);
                return true;
            }
            return false;
        }

        public int size() {
            return TreeMap.this.size();
        }

        public void clear() {
            TreeMap.this.clear();
        }

        public Spliterator<Map.Entry<K,V>> spliterator() {
            return new EntrySpliterator<>(TreeMap.this, null, null, 0, -1, 0);
        }
    }

    /*
     * Unlike Values and EntrySet, the KeySet class is static,
     * delegating to a NavigableMap to allow use by SubMaps, which
     * outweighs the ugliness of needing type-tests for the following
     * Iterator methods that are defined appropriately in main versus
     * submap classes.
     */

    Iterator<K> keyIterator() {  // 获得 key 的正序迭代器
        return new KeyIterator(getFirstEntry());   // 获得的是首个元素
    }

    Iterator<K> descendingKeyIterator() {  // 获得 key 的倒序迭代器
        return new DescendingKeyIterator(getLastEntry());   // 获得的是尾部元素
    }

    static final class KeySet<E> extends AbstractSet<E> implements NavigableSet<E> {
        private final NavigableMap<E, ?> m;
        KeySet(NavigableMap<E,?> map) { m = map; }

        public Iterator<E> iterator() {
            if (m instanceof TreeMap)
                return ((TreeMap<E,?>)m).keyIterator();
            else
                return ((TreeMap.NavigableSubMap<E,?>)m).keyIterator();
        }

        public Iterator<E> descendingIterator() {
            if (m instanceof TreeMap)
                return ((TreeMap<E,?>)m).descendingKeyIterator();
            else
                return ((TreeMap.NavigableSubMap<E,?>)m).descendingKeyIterator();
        }

        public int size() { return m.size(); }
        public boolean isEmpty() { return m.isEmpty(); }
        public boolean contains(Object o) { return m.containsKey(o); }
        public void clear() { m.clear(); }
        public E lower(E e) { return m.lowerKey(e); }
        public E floor(E e) { return m.floorKey(e); }
        public E ceiling(E e) { return m.ceilingKey(e); }
        public E higher(E e) { return m.higherKey(e); }
        public E first() { return m.firstKey(); }
        public E last() { return m.lastKey(); }
        public Comparator<? super E> comparator() { return m.comparator(); }
        public E pollFirst() {
            Map.Entry<E,?> e = m.pollFirstEntry();
            return (e == null) ? null : e.getKey();
        }
        public E pollLast() {
            Map.Entry<E,?> e = m.pollLastEntry();
            return (e == null) ? null : e.getKey();
        }
        public boolean remove(Object o) {
            int oldSize = size();
            m.remove(o);
            return size() != oldSize;
        }
        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive,
                                      E toElement,   boolean toInclusive) {
            return new KeySet<>(m.subMap(fromElement, fromInclusive,
                                          toElement,   toInclusive));
        }
        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return new KeySet<>(m.headMap(toElement, inclusive));
        }
        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return new KeySet<>(m.tailMap(fromElement, inclusive));
        }
        public SortedSet<E> subSet(E fromElement, E toElement) {
            return subSet(fromElement, true, toElement, false);
        }
        public SortedSet<E> headSet(E toElement) {
            return headSet(toElement, false);
        }
        public SortedSet<E> tailSet(E fromElement) {
            return tailSet(fromElement, true);
        }
        public NavigableSet<E> descendingSet() {
            return new KeySet<>(m.descendingMap());
        }

        public Spliterator<E> spliterator() {
            return keySpliteratorFor(m);
        }
    }

    /**
     * 实现 Iterator 接口，提供了 TreeMap 的通用实现 Iterator 的抽象类
     *
     * Base class for TreeMap Iterators
     */
    abstract class PrivateEntryIterator<T> implements Iterator<T> {
        /**
         * 下一个节点
         */
        Entry<K,V> next;
        /**
         * 最后返回的节点
         */
        Entry<K,V> lastReturned;
        /**
         * 当前的修改次数
         */
        int expectedModCount;

        PrivateEntryIterator(Entry<K,V> first) {
            expectedModCount = modCount;
            lastReturned = null;
            next = first;
        }

        public final boolean hasNext() {
            return next != null;
        }

        final Entry<K,V> nextEntry() {    // 获得下一个 Entry 节点
            // 记录当前节点
            Entry<K,V> e = next;
            // 如果没有下一个，抛出 NoSuchElementException 异常
            if (e == null)
                throw new NoSuchElementException();
            // 如果发生了修改，抛出 ConcurrentModificationException 异常
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            // 获得 e 的后继节点，赋值给 next
            next = successor(e);
            // 记录最后返回的节点
            lastReturned = e;
            // 返回当前节点
            return e;
        }

        final Entry<K,V> prevEntry() {    // 获得前一个 Entry 节点
            // 记录当前节点
            Entry<K,V> e = next;
            // 如果没有下一个，抛出 NoSuchElementException 异常
            if (e == null)
                throw new NoSuchElementException();
            // 如果发生了修改，抛出 ConcurrentModificationException 异常
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            // 获得 e 的前继节点，赋值给 next
            next = predecessor(e);
            // 记录最后返回的节点
            lastReturned = e;
            // 返回当前节点
            return e;
        }

        public void remove() {   // 删除节点
            // 如果当前返回的节点不存在，则抛出 IllegalStateException 异常
            if (lastReturned == null)
                throw new IllegalStateException();
            // 如果发生了修改，抛出 ConcurrentModificationException 异常
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            // deleted entries are replaced by their successors
            // 在 lastReturned 左右节点都存在的时候，实际在 deleteEntry 方法中，是将后继节点替换到 lastReturned 中
            // 因此，next 需要指向 lastReturned
            if (lastReturned.left != null && lastReturned.right != null)
                next = lastReturned;
            // 删除节点
            deleteEntry(lastReturned);
            // 记录新的修改次数
            expectedModCount = modCount;
            // 置空 lastReturned
            lastReturned = null;
        }
    }

    final class EntryIterator extends PrivateEntryIterator<Map.Entry<K,V>> {
        EntryIterator(Entry<K,V> first) {
            super(first);
        }
        // 实现 next 方法，实现正序
        public Map.Entry<K,V> next() {
            return nextEntry();
        }
    }

    final class ValueIterator extends PrivateEntryIterator<V> {
        ValueIterator(Entry<K,V> first) {
            super(first);
        }
        // 实现 next 方法，实现正序
        public V next() {
            return nextEntry().value;
        }
    }

    final class KeyIterator extends PrivateEntryIterator<K> {
        KeyIterator(Entry<K,V> first) {
            super(first);
        }
        // 实现 next 方法，实现正序
        public K next() {
            return nextEntry().key;
        }
    }

    final class DescendingKeyIterator extends PrivateEntryIterator<K> {
        DescendingKeyIterator(Entry<K,V> first) {
            super(first);
        }
        // 实现 next 方法，实现倒序
        public K next() {
            return prevEntry().key;
        }
        // 重写 remove 方法，因为在 deleteEntry 方法中，在 lastReturned 左右节点都存在的时候，是将后继节点替换到 lastReturned 中。
        // 而这个逻辑，对于倒序遍历，没有影响。
        public void remove() {
            // 如果当前返回的节点不存在，则抛出 IllegalStateException 异常
            if (lastReturned == null)
                throw new IllegalStateException();
            // 如果发生了修改，抛出 ConcurrentModificationException 异常
            if (modCount != expectedModCount)
                throw new ConcurrentModificationException();
            // 删除节点
            deleteEntry(lastReturned);
            // 置空 lastReturned
            lastReturned = null;
            // 记录新的修改次数
            expectedModCount = modCount;
        }
    }

    // Little utilities

    /**
     * Compares two keys using the correct comparison method for this TreeMap.
     */
    @SuppressWarnings("unchecked")
    final int compare(Object k1, Object k2) {
        return comparator==null ? ((Comparable<? super K>)k1).compareTo((K)k2)  // 如果没有自定义 comparator 比较器，则使用 key 自身比较
            : comparator.compare((K)k1, (K)k2);  // 如果有自定义 comparator 比较器，则使用它来比较。
    }

    /**
     * Test two values for equality.  Differs from o1.equals(o2) only in
     * that it copes with {@code null} o1 properly.
     */
    static final boolean valEquals(Object o1, Object o2) {
        return (o1==null ? o2==null : o1.equals(o2));
    }

    /**
     * Return SimpleImmutableEntry for entry, or null if null
     */
    static <K,V> Map.Entry<K,V> exportEntry(TreeMap.Entry<K,V> e) {
        return (e == null) ? null :
            new AbstractMap.SimpleImmutableEntry<>(e);
    }

    /**
     * Return key for entry, or null if null
     */
    static <K,V> K keyOrNull(TreeMap.Entry<K,V> e) {
        return (e == null) ? null : e.key;
    }

    /**
     * Returns the key corresponding to the specified Entry.
     * @throws NoSuchElementException if the Entry is null
     */
    static <K> K key(Entry<K,?> e) {
        if (e==null)  // 如果不存在 e 元素，则抛出 NoSuchElementException 异常
            throw new NoSuchElementException();
        return e.key;
    }


    // SubMaps

    /**
     * Dummy value serving as unmatchable fence key for unbounded
     * SubMapIterators
     */
    private static final Object UNBOUNDED = new Object();

    /**
     * @serial include
     */
    abstract static class NavigableSubMap<K,V> extends AbstractMap<K,V>
        implements NavigableMap<K,V>, java.io.Serializable {
        @java.io.Serial
        private static final long serialVersionUID = -2102997345730753016L;
        /**
         * The backing map.
         */
        final TreeMap<K,V> m;

        /**
         * lo - 开始位置
         * hi - 结束位置
         */
        /**
         * Endpoints are represented as triples (fromStart, lo,
         * loInclusive) and (toEnd, hi, hiInclusive). If fromStart is
         * true, then the low (absolute) bound is the start of the
         * backing map, and the other values are ignored. Otherwise,
         * if loInclusive is true, lo is the inclusive bound, else lo
         * is the exclusive bound. Similarly for the upper bound.
         */
        @SuppressWarnings("serial") // Conditionally serializable
        final K lo;
        @SuppressWarnings("serial") // Conditionally serializable
        final K hi;
        /**
         * fromStart - 是否从 TreeMap 开头开始。如果是的话，{@link #lo} 可以不传
         * toEnd - 是否从 TreeMap 结尾结束。如果是的话，{@link #hi} 可以不传
         */
        final boolean fromStart, toEnd;
        /**
         * loInclusive - 是否包含 key 为 {@link #lo}  的元素
         * hiInclusive - 是否包含 key 为 {@link #hi} 的元素
         */
        final boolean loInclusive, hiInclusive;

        NavigableSubMap(TreeMap<K,V> m,
                        boolean fromStart, K lo, boolean loInclusive,
                        boolean toEnd,     K hi, boolean hiInclusive) {
            // 如果既不从开头开始，又不从结尾结束，那么就要校验 lo 小于 hi ，否则抛出 IllegalArgumentException 异常
            if (!fromStart && !toEnd) {
                if (m.compare(lo, hi) > 0)
                    throw new IllegalArgumentException("fromKey > toKey");
            } else {
                // 如果不从开头开始，则进行 lo 的类型校验
                if (!fromStart) // type check
                    m.compare(lo, lo);
                // 如果不从结尾结束，则进行 hi 的类型校验
                if (!toEnd)
                    m.compare(hi, hi);
            }

            // 赋值属性
            this.m = m;
            this.fromStart = fromStart;
            this.lo = lo;
            this.loInclusive = loInclusive;
            this.toEnd = toEnd;
            this.hi = hi;
            this.hiInclusive = hiInclusive;
        }

        // internal utilities

        final boolean tooLow(Object key) {   // 判断 key 是否小于 NavigableSubMap 的开始位置的 key
            if (!fromStart) {
                // 比较 key
                int c = m.compare(key, lo);
                if (c < 0   // 如果小于，则肯定过小
                        || (c == 0 && !loInclusive))  // 如果相等，则进一步判断是否 !loInclusive ，不包含 lo 的情况
                    return true;
            }
            return false;
        }

        final boolean tooHigh(Object key) {  // 判断 key 是否大于 NavigableSubMap 的结束位置的 key
            if (!toEnd) {
                // 比较 key
                int c = m.compare(key, hi);
                if (c > 0  // 如果大于，则肯定过大
                        || (c == 0 && !hiInclusive))   // 如果相等，则进一步判断是否 !hiInclusive ，不包含 high 的情况
                    return true;
            }
            return false;
        }

        final boolean inRange(Object key) {   // 校验传入的 key 是否在子范围中
            return !tooLow(key) && !tooHigh(key);
        }

        final boolean inClosedRange(Object key) {    // 判断是否在闭合的范围内
            return (fromStart || m.compare(key, lo) >= 0)
                && (toEnd || m.compare(hi, key) >= 0);
        }

        final boolean inRange(Object key, boolean inclusive) {
            return inclusive ? inRange(key) : inClosedRange(key);
        }

        /*
         * Absolute versions of relation operations.
         * Subclasses map to these using like-named "sub"
         * versions that invert senses for descending maps
         */

        final TreeMap.Entry<K,V> absLowest() {  // 获得 NavigableSubMap 开始位置的 Entry 节点
            TreeMap.Entry<K,V> e =
                (fromStart ?  m.getFirstEntry() :   // 如果从 TreeMap 开始，则获得 TreeMap 的首个 Entry 节点
                 (loInclusive ? m.getCeilingEntry(lo) :   // 如果 key 从 lo 开始（包含），则获得 TreeMap 从 lo 开始（>=）最接近的 Entry 节点
                                m.getHigherEntry(lo)));   // 如果 key 从 lo 开始（不包含），则获得 TreeMap 从 lo 开始(>)最接近的 Entry 节点
            return (e == null || tooHigh(e.key)) /** 超过 key 过大 **/ ? null : e;
        }

        final TreeMap.Entry<K,V> absHighest() {  // 获得 NavigableSubMap 结束位置的 Entry 节点
            TreeMap.Entry<K,V> e =
                (toEnd ?  m.getLastEntry() :   // 如果从 TreeMap 开始，则获得 TreeMap 的尾部 Entry 节点
                 (hiInclusive ?  m.getFloorEntry(hi) :   // 如果 key 从 hi 开始（包含），则获得 TreeMap 从 hi 开始（<=）最接近的 Entry 节点
                                 m.getLowerEntry(hi)));  // 如果 key 从 lo 开始（不包含），则获得 TreeMap 从 lo 开始(<)最接近的 Entry 节点
            return (e == null || tooLow(e.key)) /** 超过 key 过小 **/ ? null : e;
        }

        final TreeMap.Entry<K,V> absCeiling(K key) {  // 获得 NavigableSubMap >= key 最接近的 Entry 节点
            // 如果 key 过小，则只能通过 `#absLowest()` 方法，获得 NavigableSubMap 开始位置的 Entry 节点
            if (tooLow(key))
                return absLowest();
            // 获得 NavigableSubMap >= key 最接近的 Entry 节点
            TreeMap.Entry<K,V> e = m.getCeilingEntry(key);
            return (e == null || tooHigh(e.key)) /** 超过 key 过大 **/ ? null : e;
        }

        final TreeMap.Entry<K,V> absHigher(K key) {   // 获得 NavigableSubMap > key 最接近的 Entry 节点
            // 如果 key 过小，则只能通过 `#absLowest()` 方法，获得 NavigableSubMap 开始位置的 Entry 节点
            if (tooLow(key))
                return absLowest();
            // 获得 NavigableSubMap > key 最接近的 Entry 节点
            TreeMap.Entry<K,V> e = m.getHigherEntry(key);
            return (e == null || tooHigh(e.key)) /** 超过 key 过大 **/ ? null : e;
        }

        final TreeMap.Entry<K,V> absFloor(K key) {   // 获得 NavigableSubMap <= key 最接近的 Entry 节点
            // 如果 key 过大，则只能通过 `#absHighest()` 方法，获得 NavigableSubMap 结束位置的 Entry 节点
            if (tooHigh(key))
                return absHighest();
            // 获得 NavigableSubMap <= key 最接近的 Entry 节点
            TreeMap.Entry<K,V> e = m.getFloorEntry(key);
            return (e == null || tooLow(e.key)) /** 超过 key 过小 **/ ? null : e;
        }

        final TreeMap.Entry<K,V> absLower(K key) {  // 获得 NavigableSubMap < key 最接近的 Entry 节点
            // 如果 key 过大，则只能通过 `#absHighest()` 方法，获得 NavigableSubMap 结束位置的 Entry 节点
            if (tooHigh(key))
                return absHighest();
            // 获得 NavigableSubMap < key 最接近的 Entry 节点
            TreeMap.Entry<K,V> e = m.getLowerEntry(key);
            return (e == null || tooLow(e.key)) /** 超过 key 过小 **/ ? null : e;
        }

        /** Returns the absolute high fence for ascending traversal */
        final TreeMap.Entry<K,V> absHighFence() {  // 获得 TreeMap 最大 key 的 Entry 节点，用于升序遍历的时候。注意，是 TreeMap 。
            // toEnd 为真时，意味着无限大，所以返回 null
            return (toEnd ? null : (hiInclusive ?
                                    m.getHigherEntry(hi) :   // 获得 TreeMap > hi 最接近的 Entry 节点。
                                    m.getCeilingEntry(hi)));   // 获得 TreeMap => hi 最接近的 Entry 节点。
        }

        /** Return the absolute low fence for descending traversal  */
        final TreeMap.Entry<K,V> absLowFence() {   // 获得 TreeMap 最小 key 的 Entry 节点，用于降序遍历的时候。注意，是 TreeMap 。
            return (fromStart ? null : (loInclusive ?
                                        m.getLowerEntry(lo) :   // 获得 TreeMap < lo 最接近的 Entry 节点。
                                        m.getFloorEntry(lo)));  // 获得 TreeMap <= lo 最接近的 Entry 节点。
        }

        // Abstract methods defined in ascending vs descending classes
        // These relay to the appropriate absolute versions

        abstract TreeMap.Entry<K,V> subLowest();
        abstract TreeMap.Entry<K,V> subHighest();
        abstract TreeMap.Entry<K,V> subCeiling(K key);
        abstract TreeMap.Entry<K,V> subHigher(K key);
        abstract TreeMap.Entry<K,V> subFloor(K key);
        abstract TreeMap.Entry<K,V> subLower(K key);

        /** Returns ascending iterator from the perspective of this submap */
        abstract Iterator<K> keyIterator();

        abstract Spliterator<K> keySpliterator();

        /** Returns descending iterator from the perspective of this submap */
        abstract Iterator<K> descendingKeyIterator();

        // public methods

        public boolean isEmpty() {
            return (fromStart && toEnd) ? m.isEmpty() : entrySet().isEmpty();
        }

        public int size() {
            return (fromStart && toEnd) ? m.size() : entrySet().size();
        }

        public final boolean containsKey(Object key) {
            return inRange(key) && m.containsKey(key);
        }

        public final V put(K key, V value) {
            // 校验 key 的范围，如果不在，则抛出 IllegalArgumentException 异常
            if (!inRange(key))
                throw new IllegalArgumentException("key out of range");
            // 执行添加单个元素
            return m.put(key, value);
        }

        public final V get(Object key) {
            return !inRange(key)   // 校验 key 的范围
                    ? null  // 如果不在，则返回 null
                    :  m.get(key);  // 执行获得单个元素
        }

        public final V remove(Object key) {
            return !inRange(key)  // 校验 key 的范围
                    ? null   // 如果不在，则返回 null
                    : m.remove(key);  // 执行移除单个元素
        }

        public final Map.Entry<K,V> ceilingEntry(K key) {
            return exportEntry(subCeiling(key));
        }

        public final K ceilingKey(K key) {
            return keyOrNull(subCeiling(key));
        }

        public final Map.Entry<K,V> higherEntry(K key) {
            return exportEntry(subHigher(key));
        }

        public final K higherKey(K key) {
            return keyOrNull(subHigher(key));
        }

        public final Map.Entry<K,V> floorEntry(K key) {
            return exportEntry(subFloor(key));
        }

        public final K floorKey(K key) {
            return keyOrNull(subFloor(key));
        }

        public final Map.Entry<K,V> lowerEntry(K key) {
            return exportEntry(subLower(key));
        }

        public final K lowerKey(K key) {
            return keyOrNull(subLower(key));
        }

        public final K firstKey() {
            return key(subLowest());
        }

        public final K lastKey() {
            return key(subHighest());
        }

        public final Map.Entry<K,V> firstEntry() {
            return exportEntry(subLowest());
        }

        public final Map.Entry<K,V> lastEntry() {
            return exportEntry(subHighest());
        }

        public final Map.Entry<K,V> pollFirstEntry() {
            // 获得 NavigableSubMap 的首个 Entry 节点
            TreeMap.Entry<K,V> e = subLowest();
            Map.Entry<K,V> result = exportEntry(e);
            // 如果存在，则进行删除。
            if (e != null)
                m.deleteEntry(e);
            return result;
        }

        public final Map.Entry<K,V> pollLastEntry() {
            // 获得 NavigableSubMap 的尾部 Entry 节点
            TreeMap.Entry<K,V> e = subHighest();
            Map.Entry<K,V> result = exportEntry(e);
            // 如果存在，则进行删除。
            if (e != null)
                m.deleteEntry(e);
            return result;
        }

        // Views
        /**
         * 倒序的 KeySet 缓存对象
         */
        transient NavigableMap<K,V> descendingMapView;

        transient EntrySetView entrySetView;
        /**
         * 正序的 KeySet 缓存对象
         */
        transient KeySet<K> navigableKeySetView;

        public final NavigableSet<K> navigableKeySet() {
            KeySet<K> nksv = navigableKeySetView;
            return (nksv != null) ? nksv :
                (navigableKeySetView = new TreeMap.KeySet<>(this));
        }

        public final Set<K> keySet() {
            return navigableKeySet();
        }

        public NavigableSet<K> descendingKeySet() {
            return descendingMap().navigableKeySet();
        }

        public final SortedMap<K,V> subMap(K fromKey, K toKey) {
            return subMap(fromKey, true, toKey, false);
        }

        public final SortedMap<K,V> headMap(K toKey) {
            return headMap(toKey, false);
        }

        public final SortedMap<K,V> tailMap(K fromKey) {
            return tailMap(fromKey, true);
        }

        // View classes

        abstract class EntrySetView extends AbstractSet<Map.Entry<K,V>> {
            private transient int size = -1, sizeModCount;

            public int size() {
                if (fromStart && toEnd)
                    return m.size();
                if (size == -1 || sizeModCount != m.modCount) {
                    sizeModCount = m.modCount;
                    size = 0;
                    Iterator<?> i = iterator();
                    while (i.hasNext()) {
                        size++;
                        i.next();
                    }
                }
                return size;
            }

            public boolean isEmpty() {
                TreeMap.Entry<K,V> n = absLowest();
                return n == null || tooHigh(n.key);
            }

            public boolean contains(Object o) {
                if (!(o instanceof Map.Entry))
                    return false;
                Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
                Object key = entry.getKey();
                if (!inRange(key))
                    return false;
                TreeMap.Entry<?,?> node = m.getEntry(key);
                return node != null &&
                    valEquals(node.getValue(), entry.getValue());
            }

            public boolean remove(Object o) {
                if (!(o instanceof Map.Entry))
                    return false;
                Map.Entry<?,?> entry = (Map.Entry<?,?>) o;
                Object key = entry.getKey();
                if (!inRange(key))
                    return false;
                TreeMap.Entry<K,V> node = m.getEntry(key);
                if (node!=null && valEquals(node.getValue(),
                                            entry.getValue())) {
                    m.deleteEntry(node);
                    return true;
                }
                return false;
            }
        }

        /**
         * Iterators for SubMaps
         */
        abstract class SubMapIterator<T> implements Iterator<T> {
            /**
             * 最后返回的节点
             */
            TreeMap.Entry<K,V> lastReturned;
            /**
             * 下一个节点
             */
            TreeMap.Entry<K,V> next;
            /**
             * 遍历的上限 key 。
             *
             * 如果遍历到该 key ，说明已经超过范围了
             */
            final Object fenceKey;
            /**
             * 当前的修改次数
             */
            int expectedModCount;

            SubMapIterator(TreeMap.Entry<K,V> first,
                           TreeMap.Entry<K,V> fence) {
                expectedModCount = m.modCount;
                lastReturned = null;
                next = first;
                fenceKey = fence == null ? UNBOUNDED /** 无界限 **/ : fence.key;
            }

            public final boolean hasNext() {   // 是否还有下一个节点
                return next != null && next.key != fenceKey;
            }

            final TreeMap.Entry<K,V> nextEntry() {    // 获得下一个 Entry 节点
                // 记录当前节点
                TreeMap.Entry<K,V> e = next;
                // 如果没有下一个，抛出 NoSuchElementException 异常
                if (e == null || e.key == fenceKey)
                    throw new NoSuchElementException();
                // 如果发生了修改，抛出 ConcurrentModificationException 异常
                if (m.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                // 获得 e 的后继节点，赋值给 next
                next = successor(e);
                // 记录最后返回的节点
                lastReturned = e;
                // 返回当前节点
                return e;
            }

            final TreeMap.Entry<K,V> prevEntry() {  // 获得前一个 Entry 节点
                // 记录当前节点
                TreeMap.Entry<K,V> e = next;
                // 如果没有下一个，抛出 NoSuchElementException 异常
                if (e == null || e.key == fenceKey)
                    throw new NoSuchElementException();
                // 如果发生了修改，抛出 ConcurrentModificationException 异常
                if (m.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                // 获得 e 的前继节点，赋值给 next
                next = predecessor(e);
                // 记录最后返回的节点
                lastReturned = e;
                // 返回当前节点
                return e;
            }

            final void removeAscending() {   // 删除节点（顺序遍历的情况下）
                // 如果当前返回的节点不存在，则抛出 IllegalStateException 异常
                if (lastReturned == null)
                    throw new IllegalStateException();
                // 如果发生了修改，抛出 ConcurrentModificationException 异常
                if (m.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                // deleted entries are replaced by their successors
                // 在 lastReturned 左右节点都存在的时候，实际在 deleteEntry 方法中，是将后继节点替换到 lastReturned 中
                // 因此，next 需要指向 lastReturned
                if (lastReturned.left != null && lastReturned.right != null)
                    next = lastReturned;
                // 删除节点
                m.deleteEntry(lastReturned);
                // 置空 lastReturned
                lastReturned = null;
                // 记录新的修改次数
                expectedModCount = m.modCount;
            }

            final void removeDescending() {   // 删除节点倒序遍历的情况下）
                // 如果当前返回的节点不存在，则抛出 IllegalStateException 异常
                if (lastReturned == null)
                    throw new IllegalStateException();
                // 如果发生了修改，抛出 ConcurrentModificationException 异常
                if (m.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
                // 删除节点
                m.deleteEntry(lastReturned);
                // 置空 lastReturned
                lastReturned = null;
                // 记录新的修改次数
                expectedModCount = m.modCount;
            }

        }

        final class SubMapEntryIterator extends SubMapIterator<Map.Entry<K,V>> {
            SubMapEntryIterator(TreeMap.Entry<K,V> first,
                                TreeMap.Entry<K,V> fence) {
                super(first, fence);
            }
            // 实现 next 方法，实现正序
            public Map.Entry<K,V> next() {
                return nextEntry();
            }
            // 实现 remove 方法，实现正序的移除方法
            public void remove() {
                removeAscending();
            }
        }

        final class DescendingSubMapEntryIterator extends SubMapIterator<Map.Entry<K,V>> {
            DescendingSubMapEntryIterator(TreeMap.Entry<K,V> last,
                                          TreeMap.Entry<K,V> fence) {
                super(last, fence);
            }

            // 实现 next 方法，实现倒序
            public Map.Entry<K,V> next() {
                return prevEntry();
            }
            // 实现 remove 方法，实现倒序的移除方法
            public void remove() {
                removeDescending();
            }
        }

        // Implement minimal Spliterator as KeySpliterator backup
        final class SubMapKeyIterator extends SubMapIterator<K>
            implements Spliterator<K> {
            SubMapKeyIterator(TreeMap.Entry<K,V> first,
                              TreeMap.Entry<K,V> fence) {
                super(first, fence);
            }
            // 实现 next 方法，实现正序
            public K next() {
                return nextEntry().key;
            }
            // 实现 remove 方法，实现正序的移除方法
            public void remove() {
                removeAscending();
            }
            public Spliterator<K> trySplit() {
                return null;
            }
            public void forEachRemaining(Consumer<? super K> action) {
                while (hasNext())
                    action.accept(next());
            }
            public boolean tryAdvance(Consumer<? super K> action) {
                if (hasNext()) {
                    action.accept(next());
                    return true;
                }
                return false;
            }
            public long estimateSize() {
                return Long.MAX_VALUE;
            }
            public int characteristics() {
                return Spliterator.DISTINCT | Spliterator.ORDERED |
                    Spliterator.SORTED;
            }
            public final Comparator<? super K>  getComparator() {
                return NavigableSubMap.this.comparator();
            }
        }

        final class DescendingSubMapKeyIterator extends SubMapIterator<K>
            implements Spliterator<K> {
            DescendingSubMapKeyIterator(TreeMap.Entry<K,V> last,
                                        TreeMap.Entry<K,V> fence) {
                super(last, fence);
            }
            // 实现 next 方法，实现倒序
            public K next() {
                return prevEntry().key;
            }
            // 实现 remove 方法，实现倒序的移除方法
            public void remove() {
                removeDescending();
            }
            public Spliterator<K> trySplit() {
                return null;
            }
            public void forEachRemaining(Consumer<? super K> action) {
                while (hasNext())
                    action.accept(next());
            }
            public boolean tryAdvance(Consumer<? super K> action) {
                if (hasNext()) {
                    action.accept(next());
                    return true;
                }
                return false;
            }
            public long estimateSize() {
                return Long.MAX_VALUE;
            }
            public int characteristics() {
                return Spliterator.DISTINCT | Spliterator.ORDERED;
            }
        }
    }

    /**
     * @serial include
     */
    static final class AscendingSubMap<K,V> extends NavigableSubMap<K,V> {
        @java.io.Serial
        private static final long serialVersionUID = 912986545866124060L;

        AscendingSubMap(TreeMap<K,V> m,
                        boolean fromStart, K lo, boolean loInclusive,
                        boolean toEnd,     K hi, boolean hiInclusive) {
            super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
        }

        public Comparator<? super K> comparator() {
            return m.comparator();
        }

        public NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
                                        K toKey,   boolean toInclusive) {
            // 如果不在范围，抛出 IllegalArgumentException 异常
            if (!inRange(fromKey, fromInclusive))
                throw new IllegalArgumentException("fromKey out of range");
            // 如果不在范围，抛出 IllegalArgumentException 异常
            if (!inRange(toKey, toInclusive))
                throw new IllegalArgumentException("toKey out of range");
            // 创建 AscendingSubMap 对象
            return new AscendingSubMap<>(m,
                                         false, fromKey, fromInclusive,
                                         false, toKey,   toInclusive);
        }

        public NavigableMap<K,V> headMap(K toKey, boolean inclusive) {
            // 如果不在范围，抛出 IllegalArgumentException 异常
            if (!inRange(toKey, inclusive))
                throw new IllegalArgumentException("toKey out of range");
            // 创建 AscendingSubMap 对象
            return new AscendingSubMap<>(m,
                                         fromStart, lo,    loInclusive,
                                         false,     toKey, inclusive);
        }

        public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
            // 如果不在范围，抛出 IllegalArgumentException 异常
            if (!inRange(fromKey, inclusive))
                throw new IllegalArgumentException("fromKey out of range");
            return new AscendingSubMap<>(m,
                                         false, fromKey, inclusive,
                                         toEnd, hi,      hiInclusive);
        }

        public NavigableMap<K,V> descendingMap() {
            // 创建 AscendingSubMap 对象
            NavigableMap<K,V> mv = descendingMapView;
            return (mv != null) ? mv :
                (descendingMapView =
                 new DescendingSubMap<>(m,
                                        fromStart, lo, loInclusive,
                                        toEnd,     hi, hiInclusive));
        }

        Iterator<K> keyIterator() {
            return new SubMapKeyIterator(absLowest(), absHighFence());
        }

        Spliterator<K> keySpliterator() {
            return new SubMapKeyIterator(absLowest(), absHighFence());
        }

        Iterator<K> descendingKeyIterator() {
            return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
        }

        final class AscendingEntrySetView extends EntrySetView {
            public Iterator<Map.Entry<K,V>> iterator() {
                return new SubMapEntryIterator(absLowest(), absHighFence());
            }
        }

        public Set<Map.Entry<K,V>> entrySet() {
            EntrySetView es = entrySetView;
            return (es != null) ? es : (entrySetView = new AscendingEntrySetView());
        }

        TreeMap.Entry<K,V> subLowest()       { return absLowest(); }
        TreeMap.Entry<K,V> subHighest()      { return absHighest(); }
        TreeMap.Entry<K,V> subCeiling(K key) { return absCeiling(key); }
        TreeMap.Entry<K,V> subHigher(K key)  { return absHigher(key); }
        TreeMap.Entry<K,V> subFloor(K key)   { return absFloor(key); }
        TreeMap.Entry<K,V> subLower(K key)   { return absLower(key); }
    }

    /**
     * @serial include
     */
    static final class DescendingSubMap<K,V>  extends NavigableSubMap<K,V> {
        @java.io.Serial
        private static final long serialVersionUID = 912986545866120460L;
        DescendingSubMap(TreeMap<K,V> m,
                        boolean fromStart, K lo, boolean loInclusive,
                        boolean toEnd,     K hi, boolean hiInclusive) {
            super(m, fromStart, lo, loInclusive, toEnd, hi, hiInclusive);
        }

        @SuppressWarnings("serial") // Conditionally serializable
        private final Comparator<? super K> reverseComparator =
            Collections.reverseOrder(m.comparator);

        public Comparator<? super K> comparator() {
            return reverseComparator;
        }

        public NavigableMap<K,V> subMap(K fromKey, boolean fromInclusive,
                                        K toKey,   boolean toInclusive) {
            // 如果不在范围，抛出 IllegalArgumentException 异常
            if (!inRange(fromKey, fromInclusive))
                throw new IllegalArgumentException("fromKey out of range");
            // 如果不在范围，抛出 IllegalArgumentException 异常
            if (!inRange(toKey, toInclusive))
                throw new IllegalArgumentException("toKey out of range");
            // 创建 DescendingSubMap 对象
            return new DescendingSubMap<>(m,
                                          false, toKey,   toInclusive,
                                          false, fromKey, fromInclusive);
        }

        public NavigableMap<K,V> headMap(K toKey, boolean inclusive) {
            // 如果不在范围，抛出 IllegalArgumentException 异常
            if (!inRange(toKey, inclusive))
                throw new IllegalArgumentException("toKey out of range");
            // 创建 DescendingSubMap 对象
            return new DescendingSubMap<>(m,
                                          false, toKey, inclusive,
                                          toEnd, hi,    hiInclusive);
        }

        public NavigableMap<K,V> tailMap(K fromKey, boolean inclusive) {
            // 如果不在范围，抛出 IllegalArgumentException 异常
            if (!inRange(fromKey, inclusive))
                throw new IllegalArgumentException("fromKey out of range");
            // 创建 DescendingSubMap 对象
            return new DescendingSubMap<>(m,
                                          fromStart, lo, loInclusive,
                                          false, fromKey, inclusive);
        }

        public NavigableMap<K,V> descendingMap() {
            NavigableMap<K,V> mv = descendingMapView;
            return (mv != null) ? mv :
                (descendingMapView =
                 new AscendingSubMap<>(m,
                                       fromStart, lo, loInclusive,
                                       toEnd,     hi, hiInclusive));
        }

        Iterator<K> keyIterator() {
            return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
        }

        Spliterator<K> keySpliterator() {
            return new DescendingSubMapKeyIterator(absHighest(), absLowFence());
        }

        Iterator<K> descendingKeyIterator() {
            return new SubMapKeyIterator(absLowest(), absHighFence());
        }

        final class DescendingEntrySetView extends EntrySetView {
            public Iterator<Map.Entry<K,V>> iterator() {
                return new DescendingSubMapEntryIterator(absHighest(), absLowFence());
            }
        }

        public Set<Map.Entry<K,V>> entrySet() {
            EntrySetView es = entrySetView;
            return (es != null) ? es : (entrySetView = new DescendingEntrySetView());
        }

        TreeMap.Entry<K,V> subLowest()       { return absHighest(); }
        TreeMap.Entry<K,V> subHighest()      { return absLowest(); }
        TreeMap.Entry<K,V> subCeiling(K key) { return absFloor(key); }
        TreeMap.Entry<K,V> subHigher(K key)  { return absLower(key); }
        TreeMap.Entry<K,V> subFloor(K key)   { return absCeiling(key); }
        TreeMap.Entry<K,V> subLower(K key)   { return absHigher(key); }
    }

    /**
     * This class exists solely for the sake of serialization
     * compatibility with previous releases of TreeMap that did not
     * support NavigableMap.  It translates an old-version SubMap into
     * a new-version AscendingSubMap. This class is never otherwise
     * used.
     *
     * @serial include
     */
    private class SubMap extends AbstractMap<K,V>
        implements SortedMap<K,V>, java.io.Serializable {
        @java.io.Serial
        private static final long serialVersionUID = -6520786458950516097L;
        private boolean fromStart = false, toEnd = false;
        @SuppressWarnings("serial") // Conditionally serializable
        private K fromKey;
        @SuppressWarnings("serial") // Conditionally serializable
        private K toKey;
        @java.io.Serial
        private Object readResolve() {
            return new AscendingSubMap<>(TreeMap.this,
                                         fromStart, fromKey, true,
                                         toEnd, toKey, false);
        }
        public Set<Map.Entry<K,V>> entrySet() { throw new InternalError(); }
        public K lastKey() { throw new InternalError(); }
        public K firstKey() { throw new InternalError(); }
        public SortedMap<K,V> subMap(K fromKey, K toKey) { throw new InternalError(); }
        public SortedMap<K,V> headMap(K toKey) { throw new InternalError(); }
        public SortedMap<K,V> tailMap(K fromKey) { throw new InternalError(); }
        public Comparator<? super K> comparator() { throw new InternalError(); }
    }


    // Red-black mechanics

    /**
     * 颜色 - 红色
     */
    private static final boolean RED   = false;
    /**
     * 颜色 - 黑色
     */
    private static final boolean BLACK = true;

    /**
     * Node in the Tree.  Doubles as a means to pass key-value pairs back to
     * user (see Map.Entry).
     */

    static final class Entry<K,V> implements Map.Entry<K,V> {
        /**
         * key 键
         */
        K key;
        /**
         * value 值
         */
        V value;
        /**
         * 左子节点
         */
        Entry<K,V> left;
        /**
         * 右子节点
         */
        Entry<K,V> right;
        /**
         * 父节点
         */
        Entry<K,V> parent;
        /**
         * 颜色
         */
        boolean color = BLACK;

        /**
         * Make a new cell with given key, value, and parent, and with
         * {@code null} child links, and BLACK color.
         */
        Entry(K key, V value, Entry<K,V> parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
        }

        /**
         * Returns the key.
         *
         * @return the key
         */
        public K getKey() {
            return key;
        }

        /**
         * Returns the value associated with the key.
         *
         * @return the value associated with the key
         */
        public V getValue() {
            return value;
        }

        /**
         * Replaces the value currently associated with the key with the given
         * value.
         *
         * @return the value associated with the key before this method was
         *         called
         */
        public V setValue(V value) {
            V oldValue = this.value;
            this.value = value;
            return oldValue;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            Map.Entry<?,?> e = (Map.Entry<?,?>)o;

            return valEquals(key,e.getKey()) && valEquals(value,e.getValue());
        }

        public int hashCode() {
            int keyHash = (key==null ? 0 : key.hashCode());
            int valueHash = (value==null ? 0 : value.hashCode());
            return keyHash ^ valueHash;
        }

        public String toString() {
            return key + "=" + value;
        }
    }

    /**
     * Returns the first Entry in the TreeMap (according to the TreeMap's
     * key-sort function).  Returns null if the TreeMap is empty.
     */
    final Entry<K,V> getFirstEntry() {
        Entry<K,V> p = root;
        if (p != null)
            // 循环，不断遍历到左子节点，直到没有左子节点
            while (p.left != null)
                p = p.left;
        return p;
    }

    /**
     * Returns the last Entry in the TreeMap (according to the TreeMap's
     * key-sort function).  Returns null if the TreeMap is empty.
     */
    final Entry<K,V> getLastEntry() {
        Entry<K,V> p = root;
        if (p != null)
            // 循环，不断遍历到右子节点，直到没有右子节点
            while (p.right != null)
                p = p.right;
        return p;
    }

    /**
     * Returns the successor of the specified Entry, or null if no such.
     */
    static <K,V> TreeMap.Entry<K,V> successor(Entry<K,V> t) {
        // <1> 如果 t 为空，则返回 null
        if (t == null)
            return null;
            // <2> 如果 t 的右子树非空，则取右子树的最小值
        else if (t.right != null) {
            // 先取右子树的根节点
            Entry<K,V> p = t.right;
            // 再取该根节点的做子树的最小值，即不断遍历左节点
            while (p.left != null)
                p = p.left;
            // 返回
            return p;
            // <3> 如果 t 的右子树为空
        } else {
            // 先获得 t 的父节点
            Entry<K,V> p = t.parent;
            // 不断向上遍历父节点，直到子节点 ch 不是父节点 p 的右子节点
            Entry<K,V> ch = t;
            while (p != null   // 还有父节点
                    && ch == p.right) {   // 继续遍历的条件，必须是子节点 ch 是父节点 p 的右子节点
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    /**
     * Returns the predecessor of the specified Entry, or null if no such.
     */
    static <K,V> Entry<K,V> predecessor(Entry<K,V> t) {
        // 如果 t 为空，则返回 null
        if (t == null)
            return null;
            // 如果 t 的左子树非空，则取左子树的最大值
        else if (t.left != null) {
            Entry<K,V> p = t.left;
            while (p.right != null)
                p = p.right;
            return p;
            // 如果 t 的左子树为空
        } else {
            // 先获得 t 的父节点
            Entry<K,V> p = t.parent;
            // 不断向上遍历父节点，直到子节点 ch 不是父节点 p 的左子节点
            Entry<K,V> ch = t;
            while (p != null   // 还有父节点
                    && ch == p.left) {   // 继续遍历的条件，必须是子节点 ch 是父节点 p 的左子节点
                ch = p;
                p = p.parent;
            }
            return p;
        }
    }

    /**
     * Balancing operations.
     *
     * Implementations of rebalancings during insertion and deletion are
     * slightly different than the CLR version.  Rather than using dummy
     * nilnodes, we use a set of accessors that deal properly with null.  They
     * are used to avoid messiness surrounding nullness checks in the main
     * algorithms.
     */

    private static <K,V> boolean colorOf(Entry<K,V> p) {
        return (p == null ? BLACK : p.color);
    }

    private static <K,V> Entry<K,V> parentOf(Entry<K,V> p) {
        return (p == null ? null: p.parent);
    }

    private static <K,V> void setColor(Entry<K,V> p, boolean c) {
        if (p != null)
            p.color = c;
    }

    private static <K,V> Entry<K,V> leftOf(Entry<K,V> p) {
        return (p == null) ? null: p.left;
    }

    private static <K,V> Entry<K,V> rightOf(Entry<K,V> p) {
        return (p == null) ? null: p.right;
    }

    /** From CLR */
    private void rotateLeft(Entry<K,V> p) {
        if (p != null) {
            Entry<K,V> r = p.right;
            p.right = r.left;
            if (r.left != null)
                r.left.parent = p;
            r.parent = p.parent;
            if (p.parent == null)
                root = r;
            else if (p.parent.left == p)
                p.parent.left = r;
            else
                p.parent.right = r;
            r.left = p;
            p.parent = r;
        }
    }

    /** From CLR */
    private void rotateRight(Entry<K,V> p) {
        if (p != null) {
            Entry<K,V> l = p.left;
            p.left = l.right;
            if (l.right != null) l.right.parent = p;
            l.parent = p.parent;
            if (p.parent == null)
                root = l;
            else if (p.parent.right == p)
                p.parent.right = l;
            else p.parent.left = l;
            l.right = p;
            p.parent = l;
        }
    }

    /** From CLR */
    private void fixAfterInsertion(Entry<K,V> x) {
        x.color = RED;

        while (x != null && x != root && x.parent.color == RED) {
            if (parentOf(x) == leftOf(parentOf(parentOf(x)))) {
                Entry<K,V> y = rightOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == rightOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateLeft(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateRight(parentOf(parentOf(x)));
                }
            } else {
                Entry<K,V> y = leftOf(parentOf(parentOf(x)));
                if (colorOf(y) == RED) {
                    setColor(parentOf(x), BLACK);
                    setColor(y, BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    x = parentOf(parentOf(x));
                } else {
                    if (x == leftOf(parentOf(x))) {
                        x = parentOf(x);
                        rotateRight(x);
                    }
                    setColor(parentOf(x), BLACK);
                    setColor(parentOf(parentOf(x)), RED);
                    rotateLeft(parentOf(parentOf(x)));
                }
            }
        }
        root.color = BLACK;
    }

    /**
     * Delete node p, and then rebalance the tree.
     */
    private void deleteEntry(Entry<K,V> p) {
        // 增加修改次数
        modCount++;
        // 减少 key-value 键值对数
        size--;

        // If strictly internal, copy successor's element to p and then make p
        // point to successor.
        // <1> 如果删除的节点 p 既有左子节点，又有右子节点，
        if (p.left != null && p.right != null) {
            // <1.1> 获得右子树的最小值
            Entry<K,V> s = successor(p);
            // <1.2> 修改 p 的 key-value 为 s 的 key-value 键值对
            p.key = s.key;
            p.value = s.value;
            // <1.3> 设置 p 指向 s 。此时，就变成删除 s 节点了。
            p = s;
        } // p has 2 children

        // <2> 获得替换节点
        // Start fixup at replacement node, if it exists.
        Entry<K,V> replacement = (p.left != null ? p.left : p.right);

        // <3> 有子节点的情况
        if (replacement != null) {
            // Link replacement to parent
            // <3.1> 替换节点的父节点，指向 p 的父节点
            replacement.parent = p.parent;
            // <3.2.1> 如果 p 的父节点为空，则说明 p 是根节点，直接 root 设置为替换节点
            if (p.parent == null)
                root = replacement;
                // <3.2.2> 如果 p 是父节点的左子节点，则 p 的父子节的左子节指向替换节点
            else if (p == p.parent.left)
                p.parent.left  = replacement;
                // <3.2.3> 如果 p 是父节点的右子节点，则 p 的父子节的右子节指向替换节点
            else
                p.parent.right = replacement;

            // Null out links so they are OK to use by fixAfterDeletion.
            // <3.3> 置空 p 的所有指向
            p.left = p.right = p.parent = null;

            // Fix replacement
            // <3.4> 如果 p 的颜色是黑色，则执行自平衡
            if (p.color == BLACK)
                fixAfterDeletion(replacement);
            // <4> 如果 p 没有父节点，说明删除的是根节点，直接置空 root 即可
        } else if (p.parent == null) { // return if we are the only node.
            root = null;
            // <5> 如果删除的没有左子树，又没有右子树
        } else { //  No children. Use self as phantom replacement and unlink.
            // <5.1> 如果 p 的颜色是黑色，则执行自平衡
            if (p.color == BLACK)
                fixAfterDeletion(p);

            // <5.2> 删除 p 和其父节点的相互指向
            if (p.parent != null) {
                // 如果 p 是父节点的左子节点，则置空父节点的左子节点
                if (p == p.parent.left)
                    p.parent.left = null;
                    // 如果 p 是父节点的右子节点，则置空父节点的右子节点
                else if (p == p.parent.right)
                    p.parent.right = null;
                // 置空 p 对父节点的指向
                p.parent = null;
            }
        }
    }

    /** From CLR */
    private void fixAfterDeletion(Entry<K,V> x) {
        while (x != root && colorOf(x) == BLACK) {
            if (x == leftOf(parentOf(x))) {
                Entry<K,V> sib = rightOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateLeft(parentOf(x));
                    sib = rightOf(parentOf(x));
                }

                if (colorOf(leftOf(sib))  == BLACK &&
                    colorOf(rightOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(rightOf(sib)) == BLACK) {
                        setColor(leftOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateRight(sib);
                        sib = rightOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(rightOf(sib), BLACK);
                    rotateLeft(parentOf(x));
                    x = root;
                }
            } else { // symmetric
                Entry<K,V> sib = leftOf(parentOf(x));

                if (colorOf(sib) == RED) {
                    setColor(sib, BLACK);
                    setColor(parentOf(x), RED);
                    rotateRight(parentOf(x));
                    sib = leftOf(parentOf(x));
                }

                if (colorOf(rightOf(sib)) == BLACK &&
                    colorOf(leftOf(sib)) == BLACK) {
                    setColor(sib, RED);
                    x = parentOf(x);
                } else {
                    if (colorOf(leftOf(sib)) == BLACK) {
                        setColor(rightOf(sib), BLACK);
                        setColor(sib, RED);
                        rotateLeft(sib);
                        sib = leftOf(parentOf(x));
                    }
                    setColor(sib, colorOf(parentOf(x)));
                    setColor(parentOf(x), BLACK);
                    setColor(leftOf(sib), BLACK);
                    rotateRight(parentOf(x));
                    x = root;
                }
            }
        }

        setColor(x, BLACK);
    }

    @java.io.Serial
    private static final long serialVersionUID = 919286545866124006L;

    /**
     * Save the state of the {@code TreeMap} instance to a stream (i.e.,
     * serialize it).
     *
     * @serialData The <em>size</em> of the TreeMap (the number of key-value
     *             mappings) is emitted (int), followed by the key (Object)
     *             and value (Object) for each key-value mapping represented
     *             by the TreeMap. The key-value mappings are emitted in
     *             key-order (as determined by the TreeMap's Comparator,
     *             or by the keys' natural ordering if the TreeMap has no
     *             Comparator).
     */
    @java.io.Serial
    private void writeObject(java.io.ObjectOutputStream s)
        throws java.io.IOException {
        // Write out the Comparator and any hidden stuff
        // 写入非静态属性、非 transient 属性
        s.defaultWriteObject();

        // Write out size (number of Mappings)
        // 写入 key-value 键值对数量
        s.writeInt(size);

        // Write out keys and values (alternating)
        // 写入具体的 key-value 键值对
        for (Map.Entry<K, V> e : entrySet()) {
            s.writeObject(e.getKey());
            s.writeObject(e.getValue());
        }
    }

    /**
     * Reconstitute the {@code TreeMap} instance from a stream (i.e.,
     * deserialize it).
     */
    @java.io.Serial
    private void readObject(final java.io.ObjectInputStream s)
        throws java.io.IOException, ClassNotFoundException {
        // Read in the Comparator and any hidden stuff
        // 读取非静态属性、非 transient 属性
        s.defaultReadObject();

        // Read in size
        // 读取 key-value 键值对数量 size
        int size = s.readInt();

        // 使用输入流，构建红黑树。
        // 因为序列化时，已经是顺序的，所以输入流也是顺序的
        buildFromSorted(size, null, s, null);
    }

    /** Intended to be called only from TreeSet.readObject */
    void readTreeSet(int size, java.io.ObjectInputStream s, V defaultVal)
        throws java.io.IOException, ClassNotFoundException {
        buildFromSorted(size, null, s, defaultVal);
    }

    /** Intended to be called only from TreeSet.addAll */
    void addAllForTreeSet(SortedSet<? extends K> set, V defaultVal) {
        try {
            buildFromSorted(set.size(), set.iterator(), null, defaultVal);
        } catch (java.io.IOException | ClassNotFoundException cannotHappen) {
        }
    }


    /**
     * Linear time tree building algorithm from sorted data.  Can accept keys
     * and/or values from iterator or stream. This leads to too many
     * parameters, but seems better than alternatives.  The four formats
     * that this method accepts are:
     *
     *    1) An iterator of Map.Entries.  (it != null, defaultVal == null).
     *    2) An iterator of keys.         (it != null, defaultVal != null).
     *    3) A stream of alternating serialized keys and values.
     *                                   (it == null, defaultVal == null).
     *    4) A stream of serialized keys. (it == null, defaultVal != null).
     *
     * It is assumed that the comparator of the TreeMap is already set prior
     * to calling this method.
     *
     * @param size the number of keys (or key-value pairs) to be read from
     *        the iterator or stream
     * @param it If non-null, new entries are created from entries
     *        or keys read from this iterator.
     * @param str If non-null, new entries are created from keys and
     *        possibly values read from this stream in serialized form.
     *        Exactly one of it and str should be non-null.
     * @param defaultVal if non-null, this default value is used for
     *        each value in the map.  If null, each value is read from
     *        iterator or stream, as described above.
     * @throws java.io.IOException propagated from stream reads. This cannot
     *         occur if str is null.
     * @throws ClassNotFoundException propagated from readObject.
     *         This cannot occur if str is null.
     */
    private void buildFromSorted(int size, Iterator<?> it,
                                 java.io.ObjectInputStream str,
                                 V defaultVal)
        throws  java.io.IOException, ClassNotFoundException {
        // <1> 设置 key-value 键值对的数量
        this.size = size;
        // <2> computeRedLevel(size) 方法，计算红黑树的高度
        // <3> 使用 m 构造红黑树，返回根节点
        root = buildFromSorted(0, 0, size-1, computeRedLevel(size),
                               it, str, defaultVal);
    }

    /**
     * Recursive "helper method" that does the real work of the
     * previous method.  Identically named parameters have
     * identical definitions.  Additional parameters are documented below.
     * It is assumed that the comparator and size fields of the TreeMap are
     * already set prior to calling this method.  (It ignores both fields.)
     *
     * @param level the current level of tree. Initial call should be 0.
     * @param lo the first element index of this subtree. Initial should be 0.
     * @param hi the last element index of this subtree.  Initial should be
     *        size-1.
     * @param redLevel the level at which nodes should be red.
     *        Must be equal to computeRedLevel for tree of this size.
     */
    @SuppressWarnings("unchecked")
    private final Entry<K,V> buildFromSorted(int level, int lo, int hi,
                                             int redLevel,
                                             Iterator<?> it,
                                             java.io.ObjectInputStream str,
                                             V defaultVal)
        throws  java.io.IOException, ClassNotFoundException {
        /*
         * Strategy: The root is the middlemost element. To get to it, we
         * have to first recursively construct the entire left subtree,
         * so as to grab all of its elements. We can then proceed with right
         * subtree.
         *
         * The lo and hi arguments are the minimum and maximum
         * indices to pull out of the iterator or stream for current subtree.
         * They are not actually indexed, we just proceed sequentially,
         * ensuring that items are extracted in corresponding order.
         */

        // <1.1> 递归结束
        if (hi < lo) return null;

        // <1.2> 计算中间值
        int mid = (lo + hi) >>> 1;

        // <2.1> 创建左子树
        Entry<K,V> left  = null;
        if (lo < mid)
            // <2.2> 递归左子树
            left = buildFromSorted(level+1, lo, mid - 1, redLevel,
                                   it, str, defaultVal);

        // extract key and/or value from iterator or stream
        // <3.1> 获得 key-value 键值对
        K key;
        V value;
        if (it != null) {   // 使用 it 迭代器，获得下一个值
            if (defaultVal==null) {
                Map.Entry<?,?> entry = (Map.Entry<?,?>)it.next();   // 从 it 获得下一个 Entry 节点
                key = (K)entry.getKey();   // 读取 key
                value = (V)entry.getValue();   // 读取 value
            } else {
                key = (K)it.next();    // 读取 key
                value = defaultVal;    // 设置 default 为 value
            }
        } else { // use stream  处理 str 流的情况
            key = (K) str.readObject();  //  从 str 读取 key 值
            value = (defaultVal != null ? defaultVal : (V) str.readObject());   // 从 str 读取 value 值
        }

        // <3.2> 创建中间节点
        Entry<K,V> middle =  new Entry<>(key, value, null);

        // <3.3> 如果到树的最大高度，则设置为红节点
        // color nodes in non-full bottommost level red
        if (level == redLevel)
            middle.color = RED;

        // <3.4> 如果左子树非空，则进行设置
        if (left != null) {
            middle.left = left;   // 当前节点，设置左子树
            left.parent = middle;   // 左子树，设置父节点为当前节点
        }

        // <4.1> 创建右子树
        if (mid < hi) {
            // <4.2> 递归右子树
            Entry<K,V> right = buildFromSorted(level+1, mid+1, hi, redLevel,
                                               it, str, defaultVal);
            // <4.3> 当前节点，设置右子树
            middle.right = right;
            // <4.3> 右子树，设置父节点为当前节点
            right.parent = middle;
        }

        // 返回当前节点
        return middle;
    }

    /**
     * Finds the level down to which to assign all nodes BLACK.  This is the
     * last `full' level of the complete binary tree produced by buildTree.
     * The remaining nodes are colored RED. (This makes a `nice' set of
     * color assignments wrt future insertions.) This level number is
     * computed by finding the number of splits needed to reach the zeroeth
     * node.
     *
     * @param size the (non-negative) number of keys in the tree to be built
     */
    private static int computeRedLevel(int size) {
        return 31 - Integer.numberOfLeadingZeros(size + 1);
    }

    /**
     * Currently, we support Spliterator-based versions only for the
     * full map, in either plain of descending form, otherwise relying
     * on defaults because size estimation for submaps would dominate
     * costs. The type tests needed to check these for key views are
     * not very nice but avoid disrupting existing class
     * structures. Callers must use plain default spliterators if this
     * returns null.
     */
    static <K> Spliterator<K> keySpliteratorFor(NavigableMap<K,?> m) {
        if (m instanceof TreeMap) {
            @SuppressWarnings("unchecked") TreeMap<K,Object> t =
                (TreeMap<K,Object>) m;
            return t.keySpliterator();
        }
        if (m instanceof DescendingSubMap) {
            @SuppressWarnings("unchecked") DescendingSubMap<K,?> dm =
                (DescendingSubMap<K,?>) m;
            TreeMap<K,?> tm = dm.m;
            if (dm == tm.descendingMap) {
                @SuppressWarnings("unchecked") TreeMap<K,Object> t =
                    (TreeMap<K,Object>) tm;
                return t.descendingKeySpliterator();
            }
        }
        @SuppressWarnings("unchecked") NavigableSubMap<K,?> sm =
            (NavigableSubMap<K,?>) m;
        return sm.keySpliterator();
    }

    final Spliterator<K> keySpliterator() {
        return new KeySpliterator<>(this, null, null, 0, -1, 0);
    }

    final Spliterator<K> descendingKeySpliterator() {
        return new DescendingKeySpliterator<>(this, null, null, 0, -2, 0);
    }

    /**
     * Base class for spliterators.  Iteration starts at a given
     * origin and continues up to but not including a given fence (or
     * null for end).  At top-level, for ascending cases, the first
     * split uses the root as left-fence/right-origin. From there,
     * right-hand splits replace the current fence with its left
     * child, also serving as origin for the split-off spliterator.
     * Left-hands are symmetric. Descending versions place the origin
     * at the end and invert ascending split rules.  This base class
     * is non-committal about directionality, or whether the top-level
     * spliterator covers the whole tree. This means that the actual
     * split mechanics are located in subclasses. Some of the subclass
     * trySplit methods are identical (except for return types), but
     * not nicely factorable.
     *
     * Currently, subclass versions exist only for the full map
     * (including descending keys via its descendingMap).  Others are
     * possible but currently not worthwhile because submaps require
     * O(n) computations to determine size, which substantially limits
     * potential speed-ups of using custom Spliterators versus default
     * mechanics.
     *
     * To boostrap initialization, external constructors use
     * negative size estimates: -1 for ascend, -2 for descend.
     */
    static class TreeMapSpliterator<K,V> {
        final TreeMap<K,V> tree;
        TreeMap.Entry<K,V> current; // traverser; initially first node in range
        TreeMap.Entry<K,V> fence;   // one past last, or null
        int side;                   // 0: top, -1: is a left split, +1: right
        int est;                    // size estimate (exact only for top-level)
        int expectedModCount;       // for CME checks

        TreeMapSpliterator(TreeMap<K,V> tree,
                           TreeMap.Entry<K,V> origin, TreeMap.Entry<K,V> fence,
                           int side, int est, int expectedModCount) {
            this.tree = tree;
            this.current = origin;
            this.fence = fence;
            this.side = side;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getEstimate() { // force initialization
            int s; TreeMap<K,V> t;
            if ((s = est) < 0) {
                if ((t = tree) != null) {
                    current = (s == -1) ? t.getFirstEntry() : t.getLastEntry();
                    s = est = t.size;
                    expectedModCount = t.modCount;
                }
                else
                    s = est = 0;
            }
            return s;
        }

        public final long estimateSize() {
            return (long)getEstimate();
        }
    }

    static final class KeySpliterator<K,V>
        extends TreeMapSpliterator<K,V>
        implements Spliterator<K> {
        KeySpliterator(TreeMap<K,V> tree,
                       TreeMap.Entry<K,V> origin, TreeMap.Entry<K,V> fence,
                       int side, int est, int expectedModCount) {
            super(tree, origin, fence, side, est, expectedModCount);
        }

        public KeySpliterator<K,V> trySplit() {
            if (est < 0)
                getEstimate(); // force initialization
            int d = side;
            TreeMap.Entry<K,V> e = current, f = fence,
                s = ((e == null || e == f) ? null :      // empty
                     (d == 0)              ? tree.root : // was top
                     (d >  0)              ? e.right :   // was right
                     (d <  0 && f != null) ? f.left :    // was left
                     null);
            if (s != null && s != e && s != f &&
                tree.compare(e.key, s.key) < 0) {        // e not already past s
                side = 1;
                return new KeySpliterator<>
                    (tree, e, current = s, -1, est >>>= 1, expectedModCount);
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null)
                throw new NullPointerException();
            if (est < 0)
                getEstimate(); // force initialization
            TreeMap.Entry<K,V> f = fence, e, p, pl;
            if ((e = current) != null && e != f) {
                current = f; // exhaust
                do {
                    action.accept(e.key);
                    if ((p = e.right) != null) {
                        while ((pl = p.left) != null)
                            p = pl;
                    }
                    else {
                        while ((p = e.parent) != null && e == p.right)
                            e = p;
                    }
                } while ((e = p) != null && e != f);
                if (tree.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            TreeMap.Entry<K,V> e;
            if (action == null)
                throw new NullPointerException();
            if (est < 0)
                getEstimate(); // force initialization
            if ((e = current) == null || e == fence)
                return false;
            current = successor(e);
            action.accept(e.key);
            if (tree.modCount != expectedModCount)
                throw new ConcurrentModificationException();
            return true;
        }

        public int characteristics() {
            return (side == 0 ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
        }

        public final Comparator<? super K>  getComparator() {
            return tree.comparator;
        }

    }

    static final class DescendingKeySpliterator<K,V>
        extends TreeMapSpliterator<K,V>
        implements Spliterator<K> {
        DescendingKeySpliterator(TreeMap<K,V> tree,
                                 TreeMap.Entry<K,V> origin, TreeMap.Entry<K,V> fence,
                                 int side, int est, int expectedModCount) {
            super(tree, origin, fence, side, est, expectedModCount);
        }

        public DescendingKeySpliterator<K,V> trySplit() {
            if (est < 0)
                getEstimate(); // force initialization
            int d = side;
            TreeMap.Entry<K,V> e = current, f = fence,
                    s = ((e == null || e == f) ? null :      // empty
                         (d == 0)              ? tree.root : // was top
                         (d <  0)              ? e.left :    // was left
                         (d >  0 && f != null) ? f.right :   // was right
                         null);
            if (s != null && s != e && s != f &&
                tree.compare(e.key, s.key) > 0) {       // e not already past s
                side = 1;
                return new DescendingKeySpliterator<>
                        (tree, e, current = s, -1, est >>>= 1, expectedModCount);
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super K> action) {
            if (action == null)
                throw new NullPointerException();
            if (est < 0)
                getEstimate(); // force initialization
            TreeMap.Entry<K,V> f = fence, e, p, pr;
            if ((e = current) != null && e != f) {
                current = f; // exhaust
                do {
                    action.accept(e.key);
                    if ((p = e.left) != null) {
                        while ((pr = p.right) != null)
                            p = pr;
                    }
                    else {
                        while ((p = e.parent) != null && e == p.left)
                            e = p;
                    }
                } while ((e = p) != null && e != f);
                if (tree.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super K> action) {
            TreeMap.Entry<K,V> e;
            if (action == null)
                throw new NullPointerException();
            if (est < 0)
                getEstimate(); // force initialization
            if ((e = current) == null || e == fence)
                return false;
            current = predecessor(e);
            action.accept(e.key);
            if (tree.modCount != expectedModCount)
                throw new ConcurrentModificationException();
            return true;
        }

        public int characteristics() {
            return (side == 0 ? Spliterator.SIZED : 0) |
                Spliterator.DISTINCT | Spliterator.ORDERED;
        }
    }

    static final class ValueSpliterator<K,V>
            extends TreeMapSpliterator<K,V>
            implements Spliterator<V> {
        ValueSpliterator(TreeMap<K,V> tree,
                         TreeMap.Entry<K,V> origin, TreeMap.Entry<K,V> fence,
                         int side, int est, int expectedModCount) {
            super(tree, origin, fence, side, est, expectedModCount);
        }

        public ValueSpliterator<K,V> trySplit() {
            if (est < 0)
                getEstimate(); // force initialization
            int d = side;
            TreeMap.Entry<K,V> e = current, f = fence,
                    s = ((e == null || e == f) ? null :      // empty
                         (d == 0)              ? tree.root : // was top
                         (d >  0)              ? e.right :   // was right
                         (d <  0 && f != null) ? f.left :    // was left
                         null);
            if (s != null && s != e && s != f &&
                tree.compare(e.key, s.key) < 0) {        // e not already past s
                side = 1;
                return new ValueSpliterator<>
                        (tree, e, current = s, -1, est >>>= 1, expectedModCount);
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super V> action) {
            if (action == null)
                throw new NullPointerException();
            if (est < 0)
                getEstimate(); // force initialization
            TreeMap.Entry<K,V> f = fence, e, p, pl;
            if ((e = current) != null && e != f) {
                current = f; // exhaust
                do {
                    action.accept(e.value);
                    if ((p = e.right) != null) {
                        while ((pl = p.left) != null)
                            p = pl;
                    }
                    else {
                        while ((p = e.parent) != null && e == p.right)
                            e = p;
                    }
                } while ((e = p) != null && e != f);
                if (tree.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super V> action) {
            TreeMap.Entry<K,V> e;
            if (action == null)
                throw new NullPointerException();
            if (est < 0)
                getEstimate(); // force initialization
            if ((e = current) == null || e == fence)
                return false;
            current = successor(e);
            action.accept(e.value);
            if (tree.modCount != expectedModCount)
                throw new ConcurrentModificationException();
            return true;
        }

        public int characteristics() {
            return (side == 0 ? Spliterator.SIZED : 0) | Spliterator.ORDERED;
        }
    }

    static final class EntrySpliterator<K,V>
        extends TreeMapSpliterator<K,V>
        implements Spliterator<Map.Entry<K,V>> {
        EntrySpliterator(TreeMap<K,V> tree,
                         TreeMap.Entry<K,V> origin, TreeMap.Entry<K,V> fence,
                         int side, int est, int expectedModCount) {
            super(tree, origin, fence, side, est, expectedModCount);
        }

        public EntrySpliterator<K,V> trySplit() {
            if (est < 0)
                getEstimate(); // force initialization
            int d = side;
            TreeMap.Entry<K,V> e = current, f = fence,
                    s = ((e == null || e == f) ? null :      // empty
                         (d == 0)              ? tree.root : // was top
                         (d >  0)              ? e.right :   // was right
                         (d <  0 && f != null) ? f.left :    // was left
                         null);
            if (s != null && s != e && s != f &&
                tree.compare(e.key, s.key) < 0) {        // e not already past s
                side = 1;
                return new EntrySpliterator<>
                        (tree, e, current = s, -1, est >>>= 1, expectedModCount);
            }
            return null;
        }

        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            if (action == null)
                throw new NullPointerException();
            if (est < 0)
                getEstimate(); // force initialization
            TreeMap.Entry<K,V> f = fence, e, p, pl;
            if ((e = current) != null && e != f) {
                current = f; // exhaust
                do {
                    action.accept(e);
                    if ((p = e.right) != null) {
                        while ((pl = p.left) != null)
                            p = pl;
                    }
                    else {
                        while ((p = e.parent) != null && e == p.right)
                            e = p;
                    }
                } while ((e = p) != null && e != f);
                if (tree.modCount != expectedModCount)
                    throw new ConcurrentModificationException();
            }
        }

        public boolean tryAdvance(Consumer<? super Map.Entry<K,V>> action) {
            TreeMap.Entry<K,V> e;
            if (action == null)
                throw new NullPointerException();
            if (est < 0)
                getEstimate(); // force initialization
            if ((e = current) == null || e == fence)
                return false;
            current = successor(e);
            action.accept(e);
            if (tree.modCount != expectedModCount)
                throw new ConcurrentModificationException();
            return true;
        }

        public int characteristics() {
            return (side == 0 ? Spliterator.SIZED : 0) |
                    Spliterator.DISTINCT | Spliterator.SORTED | Spliterator.ORDERED;
        }

        @Override
        public Comparator<Map.Entry<K, V>> getComparator() {
            // Adapt or create a key-based comparator
            if (tree.comparator != null) {
                return Map.Entry.comparingByKey(tree.comparator);
            }
            else {
                return (Comparator<Map.Entry<K, V>> & Serializable) (e1, e2) -> {
                    @SuppressWarnings("unchecked")
                    Comparable<? super K> k1 = (Comparable<? super K>) e1.getKey();
                    return k1.compareTo(e2.getKey());
                };
            }
        }
    }
}
