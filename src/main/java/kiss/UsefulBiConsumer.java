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

import java.util.function.BiConsumer;

/**
 * @version 2017/03/13 9:34:23
 */
public interface UsefulBiConsumer<Param1, Param2> extends BiConsumer<Param1, Param2>, UsefulLambda {

    /**
     * <p>
     * Internal API.
     * </p>
     * 
     * @param param1 A proxy parameter.
     * @param param2 A proxy parameter.
     * @throws Throwable A sneaky exception for lambda.
     */
    void ACCEPT(Param1 param1, Param2 param2) throws Throwable;

    /**
     * {@inheritDoc}
     */
    @Override
    default void accept(Param1 param1, Param2 param2) {
        try {
            ACCEPT(param1, param2);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }
}
