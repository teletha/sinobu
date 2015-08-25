/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.lambda;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @version 2015/08/25 16:31:20
 */
public interface NamedValue<V> extends Function<String, V>, Serializable {

    default String name() {
        return MethodFinder.method(this).getParameters()[0].getName();
    }

    default V value() {
        return apply(null);
    }
}
