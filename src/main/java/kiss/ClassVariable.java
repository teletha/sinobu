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
public class ClassVariable<T> extends ClassValue<T> {

    /** The variable type. */
    private final boolean invariance;

    /** The placeholder. */
    private T value;

    /**
     * <p>
     * </p>
     * 
     * @param invariance
     */
    public ClassVariable(boolean invariance) {
        this.invariance = invariance;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected T computeValue(Class type) {
        return value;
    }

    /**
     * <p>
     * Set the value for the given {@link Class}. If some value has been associateed already and
     * this variable is variance, the given value will override the current value.
     * </p>
     * 
     * @param type
     * @param value
     * @return The current value.
     */
    public synchronized T set(Class type, T value) {
        T current = get(type);

        // ignore null
        if (value != null && (!invariance || current == null)) {
            // remove the current value
            remove(type);

            // then, set new value
            this.value = value;
            current = get(type);
            this.value = null;
        }
        return current;
    }
}
