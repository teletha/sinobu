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
 * @version 2014/07/09 9:58:00
 */
public class ClassLocal<T> extends ClassValue<T> {

    private Map<Class, T> values = new HashMap();

    /** The current supplier. */
    private Function<Class, T> supplier;

    /**
     * {@inheritDoc}
     */
    @Override
    protected T computeValue(Class type) {
        T value = values.remove(type);

        if (value != null) {
            return value;
        }
        return supplier.apply(type);
    }

    /**
     * <p>
     * Returns the value for the given class. If no value has yet been computed, it is obtained by
     * the given value;
     * </p>
     * 
     * @param type The type whose class value must be computed or retrieved.
     * @param value The value supplier.
     * @return The current value associated with this ClassValue, for the given class or interface.
     */
    public T get(Class type, T value) {
        return get(type, clazz -> {
            return value;
        });
    }

    /**
     * <p>
     * Returns the value for the given class. If no value has yet been computed, it is obtained by
     * an invocation of the {@link Function#apply(Object)} method.
     * </p>
     * <p>
     * he actual installation of the value on the class is performed atomically. At that point, if
     * several racing threads have computed values, one is chosen, and returned to all the racing
     * threads.
     * </p>
     * 
     * @param type The type whose class value must be computed or retrieved.
     * @param supplier The value supplier.
     * @return The current value associated with this ClassValue, for the given class or interface.
     */
    public T get(Class type, Function<Class, T> supplier) {
        this.supplier = supplier;

        return get(type);
    }

    public void set(Class type, T value) {
        values.put(type, value);
    }
}
