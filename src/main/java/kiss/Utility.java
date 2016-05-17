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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @version 2016/05/16 9:43:04
 */
public class Utility {

    public static void main(String[] args) {
        Associator<String, String> map = makeMultiMap(false);
        map.join("test", "1");
        map.join("test", "2");
        map.join("test", "1");

        System.out.println(map);
        System.out.println(map.find("test"));

        map.unjoin("test", "1");

        System.out.println(map);
        System.out.println(map.find("test"));
    }

    private static <Multimap extends Map<K, List<V>> & Associator<K, V>, K, V> Multimap makeMultiMap(boolean allowDuplication) {
        return (Multimap) new Multi(allowDuplication);
    }

    /**
     * @version 2016/05/16 9:48:56
     */
    @SuppressWarnings("serial")
    private static class Multi<K, V> extends ConcurrentHashMap<K, CopyOnWriteArrayList<V>> implements Associator<K, V> {

        /** The store builder. */
        private final boolean duplication;

        /**
         * @param duplication
         */
        private Multi(boolean duplication) {
            this.duplication = duplication;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V find(K key) {
            CopyOnWriteArrayList<V> list = get(key);

            int size = list.size();

            if (size != 0) {
                try {
                    return list.get(list.size() - 1);
                } catch (IndexOutOfBoundsException e) {
                    // list elements were removed just after executing size method
                    return find(key);
                }
            }

            // API definition
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Associator<K, V> join(K key, V value) {
            CopyOnWriteArrayList<V> list = computeIfAbsent(key, k -> new CopyOnWriteArrayList());

            if (duplication) {
                list.add(value);
            } else {
                list.addIfAbsent(value);
            }

            // API definition
            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Associator<K, V> unjoin(K key, V value) {
            CopyOnWriteArrayList<V> list = get(key);

            // unregister
            list.remove(value);

            // remove if the specified property's pool is empty
            if (list.size() == 0) {
                remove(key, list);
            }

            // API definition
            return this;
        }
    }
}
