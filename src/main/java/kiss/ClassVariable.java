/*
 * Copyright (C) 2015 Nameless Production Committee
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
 * @version 2015/08/26 2:49:01
 */
public class ClassVariable<V> extends ClassValue<V> {

    private Function<Class, V> supplier;

    /**
     * @param supplier
     */
    public ClassVariable(Function<Class, V> supplier) {
        this.supplier = supplier;
    }

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
}
