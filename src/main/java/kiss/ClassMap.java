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

/**
 * @version 2014/01/30 15:34:37
 */
public class ClassMap<T> extends ClassValue<T> {

    /** The initial value holder. */
    private Map<Class, T> values = new HashMap();

    /**
     * {@inheritDoc}
     */
    @Override
    protected T computeValue(Class<?> type) {
        return values.remove(type);
    }

    /**
     * <p>
     * If the specified key is not already associated with a value (or is mapped to null) associates
     * it with the given value and returns null, else returns the current value.
     * </p>
     * 
     * @param type A key with which the specified value is to be associated.
     * @param value A value to be associated with the specified key.
     * @return A previous value associated with the specified key, or null if there was no mapping
     *         for the key. (A null return can also indicate that the map previously associated null
     *         with the key, if the implementation supports null values.)
     */
    public T put(Class type, T value) {
        T prev = get(type);

        if (prev == null) {
            remove(type);
            values.putIfAbsent(type, value);
        }
        return get(type);
    }
}
