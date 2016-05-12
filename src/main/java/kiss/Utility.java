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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @version 2016/05/12 16:12:56
 */
public class Utility {

    public static <K, V, Container extends Collection<V>, MultiMap extends Map<K, Container> & Associator<K, V>> MultiMap create(Container... typeInfer) {
        Class<Container> componentType = (Class<Container>) typeInfer.getClass().getComponentType();
        MMap<Object, V, Container> mMap = new MMap<>(() -> I.make(componentType));
        return mMap;
    }

    public static void main(String[] args) {
        Map<String, List<String>> create = create();
        System.out.println(create);
    }

    /**
     * @version 2016/05/12 16:17:39
     */
    @SuppressWarnings("serial")
    public static class MMap<K, V, Container extends Collection<V>> extends ConcurrentHashMap<K, Container> implements Associator<K, V> {

        private final Supplier<Container> builder;

        /**
         * @param builder
         */
        public MMap(Supplier<Container> builder) {
            this.builder = builder;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public V find(K key) {
            return null;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean bind(K key, V value) {
            return false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean unbind(K key, V value) {
            return false;
        }
    }
}
