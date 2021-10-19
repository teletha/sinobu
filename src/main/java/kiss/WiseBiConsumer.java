/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.function.BiConsumer;

public interface WiseBiConsumer<Param1, Param2>
        extends BiConsumer<Param1, Param2>, Narrow<WiseConsumer<Param2>, Param1, WiseConsumer<Param1>, Param2> {

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
     * {@inheritDoc}
     */
    @Override
    default Param1 invoke(Object... params) {
        accept((Param1) params[0], (Param2) params[1]);
        return (Param1) params[0];
    }
}