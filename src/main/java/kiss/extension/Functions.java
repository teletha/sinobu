/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.extension;

import java.util.function.Function;
import java.util.function.Supplier;

import bee.extension.Extension;

/**
 * @version 2016/12/13 11:50:04
 */
@Extension
public class Functions {

    /**
     * <p>
     * Apply parameter partialy.
     * </p>
     * 
     * @param function A target function to apply parameter.
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    @Extension.Method
    public static <P, R> Supplier<R> with(Function<P, R> function, P param) {
        return () -> function.apply(param);
    }
}
