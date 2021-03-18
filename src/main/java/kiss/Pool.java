/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * This is a crazy simple thread-safed object pool. A freshly created pool is empty. When an object
 * is requested ({@link #get()}), it returns the object if it exists in the pool, or a newly created
 * one if it does not. When an object is returned to the pool ({@link #accept(Object)}), it will be
 * ignored if the pool is full, otherwise it will be initialized and stored.
 */
public class Pool<T> implements Supplier<T>, Consumer<T> {

    private final Queue<T> pool;

    private final WiseSupplier<T> builder;

    private final Consumer<T> cleaner;

    /**
     * Create the new object pool.
     * 
     * @param size A maximum number to cache.
     * @param builder How to create a new object.
     * @param cleaner How to initialize an object.
     */
    public Pool(int size, WiseSupplier<T> builder, Consumer<T> cleaner) {
        this.pool = new ArrayBlockingQueue(size);
        this.builder = builder;
        this.cleaner = cleaner;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get() {
        T item = pool.poll();
        if (item == null) {
            item = builder.get();
        }
        return item;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void accept(T item) {
        if (item != null) {
            if (cleaner != null) cleaner.accept(item);
            pool.offer(item);
        }
    }
}
