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
 * @version 2015/08/27 10:47:03
 */
public interface NamedValue<V> extends Function<String, V> {

    /**
     * <p>
     * Retrieve the key.
     * </p>
     * 
     * @return A key string.
     */
    default String name() {
        return I.method(this);
    }

    /**
     * <p>
     * Retrieve the value.
     * </p>
     * 
     * @return A value.
     */
    default V value() {
        return apply(null);
    }
}
