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

/**
 * @version 2014/07/15 19:03:30
 */
public class ClassVariable<T> extends ClassValue {

    /**
     * {@inheritDoc}
     */
    @Override
    protected Object computeValue(Class type) {
        return new Agent();
    }

    /**
     * <p>
     * Set the value for the given {@link Class}. If some value has been associateed already and
     * this variable is variance, the given value will override the current value.
     * </p>
     * 
     * @param type A class type as key.
     * @param value An associated value.
     * @return The current value.
     */
    public synchronized T set(Class type, T value) {
        Agent<T> holder = (Agent<T>) super.get(type);

        if (value != null && holder.index == 0) {
            holder.object = value;
        }
        return holder.object;
    }

    /**
     * <p>
     * Set the value for the given {@link Class}. If some value has been associateed already and
     * this variable is variance, the given value will override the current value.
     * </p>
     * 
     * @param type A class type as key.
     * @param value An associated value.
     * @return The current value.
     */
    public synchronized T let(Class type, T value) {
        Agent<T> holder = (Agent<T>) super.get(type);

        if (value != null && holder.index == 0) {
            holder.index = 1;
            holder.object = value;
        }
        return holder.object;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T get(Class type) {
        return ((Agent<T>) super.get(type)).object;
    }
}
