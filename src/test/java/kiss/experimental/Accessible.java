/*
 * Copyright (C) 2022 The SINOBU Development Team
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

public interface Accessible {

    /**
     * Returns the value type.
     * 
     * @return A value type.
     */
    Model type();

    /**
     * Return the human readable identifier.
     * 
     * @return An identifier.
     */
    String name();

    /**
     * Check whether this value is transient or not.
     * 
     * @return A result.
     */
    boolean isTransient();

    /**
     * Retrieve a property accessor.
     * 
     * @return A property accessor.
     */
    MethodHandle getter();

    /**
     * Retrieve a property accessor.
     * 
     * @return A property accessor.
     */
    MethodHandle setter();
}