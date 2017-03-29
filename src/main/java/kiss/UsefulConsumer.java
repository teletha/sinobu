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
 * @version 2017/03/13 9:34:23
 */
public interface UsefulConsumer<P> extends Consumer<P>, UsefulLambda {

    /**
     * <p>
     * Internal API.
     * </p>
     * 
     * @param param A proxy parameter.
     * @throws Throwable A sneaky exception for lambda.
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
