/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p>
 * This class is designed specifically for the holder of listeners.
 * </p>
 * <p>
 * A multi hash table supporting full concurrency of retrievals and adjustable expected concurrency
 * for updates. This class obeys the same functional specification as {@link java.util.Hashtable},
 * and includes versions of methods corresponding to each method of HashTable. However, even though
 * all operations are thread-safe, retrieval operations do not entail locking, and there is not any
 * support for locking the entire table in a way that prevents all access. This class is fully
 * interoperable with Hashtable in programs that rely on its thread safety but not on its
 * synchronization details.
 * </p>
 * <p>
 * Retrieval operations (including get and find) generally do not block, so may overlap with update
 * operations (including put and remove). Retrievals reflect the results of the most recently
 * completed update operations holding upon their onset. For aggregate operations such as putAll and
 * clear, concurrent retrievals may reflect insertion or removal of only some entries. Similarly,
 * Iterators and Enumerations return elements reflecting the state of the hash table at some point
 * at or since the creation of the iterator/enumeration. They do not throw
 * {@link java.util.ConcurrentModificationException}. However, iterators are designed to be used by
 * only one thread at a time.
 * </p>
 * <p>
 * Implementation of {@link Table} that uses an {@link java.util.List} to store the values for a
 * given key. A {@link ConcurrentHashMap} associates each key with an {@link CopyOnWriteArrayList}
 * of values.
 * </p>
 * <p>
 * When iterating through the collections supplied by this class, the ordering of values for a given
 * key agrees with the order in which the values were added.
 * </p>
 * <p>
 * The listeners does not store duplicate key-value pairs. Adding a new key-value pair equal to an
 * existing key-value pair has no effect.
 * </p>
 * <p>
 * This class does not allow <code>null</code> to be used as a key.
 * </p>
 * 
 * @param <K> A type of key.
 * @param <V> A type of value.
 * @version 2010/02/19 2:30:56
 */
@SuppressWarnings({"serial", "unchecked"})
public class Table<K, V> extends ConcurrentHashMap<K, List<V>> {

    /**
     * <p>
     * Returns a {@link List} view of all values associated with a key. If no mappings in the
     * multimap have the provided key, an empty list is returned.
     * </p>
     * <p>
     * Changes to the returned list will update the underlying map, and vice versa.
     * </p>
     * 
     * @see java.util.concurrent.ConcurrentHashMap#get(java.lang.Object)
     */
    @Override
    public List<V> get(Object key) {
        List<V> list = super.get(key);

        // API definition
        return list == null ? Collections.EMPTY_LIST : list;
    }

    /**
     * <p>
     * Returns the last added value to which the specified key is mapped, or <code>null</code> if
     * this map contains no mapping for the key.
     * </p>
     * <p>
     * More formally, if this map contains a mapping from a key k to a value v such that
     * key.equals(k), then this method returns v, otherwise it returns <code>null</code>.
     * </p>
     * 
     * @param key A key whose associated value is to be returned.
     * @return A last added value to which the specified key is mapped, or <code>null</code> if this
     *         map contains no mapping for the key.
     * @throws NullPointerException If the specified key is <code>null</code>.
     */
    public V find(K key) {
        List<V> list = get(key);

        int size = list.size();

        if (size != 0) {
            try {
                return list.get(size - 1);
            } catch (IndexOutOfBoundsException e) {
                // list elements were removed just after executing size method
                return find(key);
            }
        }

        // API definition
        return null;
    }

    /**
     * <p>
     * Stores a key-value pair in the multimap. The key may <em>not</em> be <code>null</code>. The
     * value may be <code>null</code>.
     * </p>
     * <p>
     * This method prohibits duplicates, and storing a key-value pair that's already in the
     * {@link Table} has no effect.
     * </p>
     * 
     * @param key A key to store in the multimap.
     * @param value A value to store in the multimap.
     * @throws NullPointerException If the specified key is <code>null</code>.
     */
    public boolean push(K key, V value) {
        // The cost of creating new CopyOnWriteArrayList instance is pretty low, so we may
        // create it each time.
        ((CopyOnWriteArrayList<V>) computeIfAbsent(key, k -> new CopyOnWriteArrayList())).addIfAbsent(value);
        List list = putIfAbsent(key, new CopyOnWriteArrayList());

        // register value if absent
        ((CopyOnWriteArrayList) get(key)).addIfAbsent(value);

        return list == null;
    }

    // /**
    // * <p>
    // * Stores a collection of values with the same key.
    // * </p>
    // *
    // * @param key A key to store in the multimap.
    // * @param values Values to store in the multimap.
    // */
    // public void putAll(K key, Collection<? extends V> values) {
    // // The cost of creating new CopyOnWriteArrayList instance is pretty low, so we may
    // // create it each time.
    // CopyOnWriteArrayList<V> list = putIfAbsent(key, new CopyOnWriteArrayList());
    //
    // if (list == null) {
    // list = get(key);
    // }
    //
    // // register value if absent
    // list.addAllAbsent(values);
    // }

    /**
     * <p>
     * Removes a key-value pair from the multimap.
     * </p>
     * 
     * @param key A key of entry to remove from the multimap.
     * @param value A value of entry to remove the multimap
     */
    public boolean pull(K key, V value) {
        List list = get(key);

        // unregister
        list.remove(value);

        // remove if the specified property's pool is empty
        if (list.size() == 0) {
            remove(key, list);
            return true;
        } else {
            return false;
        }
    }

    // /**
    // * <p>
    // * Removes a key-value pair from the multimap.
    // * </p>
    // *
    // * @param key A key of entry to remove from the multimap.
    // * @param value A value of entry to remove the multimap
    // */
    // public void removeAll(K key, Class clazz) {
    // CopyOnWriteArrayList<V> list = get(key);
    //
    // // unregister
    // if (list != null) {
    // for (V value : list) {
    // if (value.getClass() == clazz) {
    // list.remove(value);
    // }
    // }
    // }
    // }
}
