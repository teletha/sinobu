/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.lambda;

/**
 * @version 2014/07/18 2:34:35
 */
public interface Consumer<T> extends java.util.function.Consumer<T> {

    Consumer Φ = param -> {
    };

    default Runnable with(T parameter) {
        return () -> accept(parameter);
    }
}
