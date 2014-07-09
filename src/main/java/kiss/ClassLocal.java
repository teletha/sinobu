/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @version 2014/07/09 8:31:15
 */
public class ClassLocal<T> extends ClassValue<T> {

    final Map<Class, T> values = new HashMap();

    private Function<Class, T> supplier;

    /**
     * {@inheritDoc}
     */
    @Override
    protected T computeValue(Class type) {
        T value = values.remove(type);

        return value != null ? value : supplier.apply(type);
    }

    public T get(Class type, Function<Class, T> supplier) {
        this.supplier = supplier;

        return get(type);
    }
}
