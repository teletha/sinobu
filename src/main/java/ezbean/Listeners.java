/*
 * Copyright (C) 2010 Nameless Production Committee.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
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
 * Implementation of MultiMap that uses an {@link java.util.List} to store the values for a given
 * key. A {@link ConcurrentHashMap} associates each key with an {@link CopyOnWriteArrayList} of
 * values.
 * </p>
 * <p>
 * When iterating through the collections supplied by this class, the ordering of values for a given
 * key agrees with the order in which the values were added.
 *</p>
 *<p>
 * The MultiMap does not store duplicate key-value pairs. Adding a new key-value pair equal to an
 * existing key-value pair has no effect.
 *</p>
 *<p>
 * This class does not allow <code>null</code> to be used as a key.
 *</p>
 * 
 * @version 2010/01/18 17:49:36
 */
@SuppressWarnings("serial")
public class Listeners<K, V> extends ConcurrentHashMap<K, CopyOnWriteArrayList<V>> {

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

        if (list != null) {
            int size = list.size();

            if (size != 0) {
                try {
                    return list.get(size - 1);
                } catch (IndexOutOfBoundsException e) {
                    // list elements were removed just after executing size method
                    return find(key);
                }
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
     * {@link Listeners} has no effect.
     * </p>
     * 
     * @param key A key to store in the multimap.
     * @param value A value to store in the multimap.
     * @throws NullPointerException If the specified key is <code>null</code>.
     */
    public void put(K key, V value) {
        // The cost of creating new CopyOnWriteArrayList instance is pretty low, so we may
        // create it each time.
        putIfAbsent(key, new CopyOnWriteArrayList());

        // register value if absent
        get(key).addIfAbsent(value);
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
    public void remove(K key, V value) {
        List list = get(key);

        // unregister
        if (list != null) {
            list.remove(value);

            // remove if the specified property's pool is empty
            if (list.size() == 0) {
                remove(key, list);
            }
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

    /**
     * <p>
     * Fire property change event to all {@link PropertyListener}s.
     * </p>
     * 
     * @param object An event source. (not be <code>null</code>)
     * @param name A name of chenged property. (not be <code>null</code>)
     * @param oldValue The old value. (may be <code>null</code>)
     * @param newValue The new value. (may be <code>null</code>)
     */
    public void notify(Object object, String name, Object oldValue, Object newValue) {
        // check diff
        if (object != null && oldValue != newValue) {
            List list = get(name);

            // fire event
            if (list != null) {
                for (Object listener : list) {
                    if (listener instanceof PropertyListener) {
                        ((PropertyListener) listener).change(object, name, oldValue, newValue);
                    }
                }
            }
        }
    }
}
