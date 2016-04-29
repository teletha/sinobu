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

import java.util.function.Function;

/**
 * @version 2016/04/21 16:42:15
 */
class ClassStore<V> extends ClassValue<V> {

    private Function<Class, V> supplier;

    /**
     * {@inheritDoc}
     */
    @Override
    protected V computeValue(Class<?> type) {
        return supplier.apply(type);
    }

    public V get(Class<?> type, Function<Class, V> supplier) {
        this.supplier = supplier;

        return get(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public V get(Class<?> type) {
        if (supplier == null) {
            throw new IllegalStateException("No supplier");
        }
        return super.get(type);
    }
}
