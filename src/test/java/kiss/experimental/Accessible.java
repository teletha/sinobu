/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.experimental;

import java.lang.invoke.MethodHandle;

import kiss.model.Model;

/**
 * @version 2014/07/21 17:13:05
 */
public interface Accessible {

    /**
     * <p>
     * Returns the value type.
     * </p>
     * 
     * @return A value type.
     */
    Model type();

    /**
     * <p>
     * Return the human readable identifier.
     * </p>
     * 
     * @return An identifier.
     */
    String name();

    /**
     * <p>
     * Check whether this value is transient or not.
     * </p>
     * 
     * @return A result.
     */
    boolean isTransient();

    /**
     * <p>
     * Retrieve a property accessor.
     * </p>
     * 
     * @param getter An accessor type.
     * @return A property accessor.
     */
    MethodHandle getter();

    /**
     * <p>
     * Retrieve a property accessor.
     * </p>
     * 
     * @param getter An accessor type.
     * @return A property accessor.
     */
    MethodHandle setter();
}
