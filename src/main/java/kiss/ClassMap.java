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
class ClassMap<V> extends ClassValue<V> {

    private final Function<Class, V> supplier;

    /**
     * @param supplier
     */
    ClassMap(Function<Class, V> supplier) {
        this.supplier = supplier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected V computeValue(Class<?> type) {
        return supplier.apply(type);
    }
}
