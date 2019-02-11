/*
 * Copyright (C) 2019 Nameless Production Committee
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
 * @version 2018/04/02 8:36:04
 */
public interface WiseBiConsumer<Param1, Param2> extends BiConsumer<Param1, Param2> {

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

    /**
     * <p>
     * Apply parameter partialy.
     * </p>
     * 
     * @param function A target function to apply parameter.
     * @param param1 A fixed parameter.
     * @return A partial applied function.
     */
    default WiseConsumer<Param2> with(Param1 param1) {
        return param2 -> ACCEPT(param1, param2);
    }
}
