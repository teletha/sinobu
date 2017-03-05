/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.function.Consumer;

/**
 * @version 2017/02/02 12:27:22
 */
public interface ThrowableConsumer<P> extends Consumer<P>, EnhancedLambda {

    /**
     * Performs this operation on the given argument.
     *
     * @param param The input argument.
     */
    void ACCEPT(P param) throws Throwable;

    /**
     * {@inheritDoc}
     */
    @Override
    default void accept(P param) {
        try {
            ACCEPT(param);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }
}
