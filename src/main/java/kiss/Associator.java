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

/**
 * @version 2016/05/16 9:34:22
 */
public interface Associator<K, V> {

    /**
     * <p>
     * Returns the intimately associated value to which the specified key is mapped, or
     * <code>null</code> if this map contains no mapping for the key.
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
    V find(K key);

    /**
     * <p>
     * Stores a key-value pair in the store. The key may <em>not</em> be <code>null</code>. The
     * value may be <code>null</code>.
     * </p>
     * <p>
     * This method prohibits duplicates, and storing a key-value pair that's already in the
     * {@link Table} has no effect.
     * </p>
     * 
     * @param key A key to store in the store.
     * @param value A value to store in the store.
     * @return Chainable API.
     * @throws NullPointerException If the specified key is <code>null</code>.
     */
    Associator<K, V> join(K key, V value);

    /**
     * <p>
     * Removes a key-value pair from the store.
     * </p>
     * 
     * @param key A key of entry to remove from the store.
     * @param value A value of entry to remove the store
     * @return Chainable API.
     */
    Associator<K, V> unjoin(K key, V value);
}
