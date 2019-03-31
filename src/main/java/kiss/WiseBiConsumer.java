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
import java.util.function.Supplier;

public interface WiseBiConsumer<Param1, Param2>
        extends BiConsumer<Param1, Param2>, Narrow<WiseConsumer<Param1>, Param2, WiseConsumer<Param2>, Param1> {

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
     * Widen parameter at last (appended parameter will be ignored).
     * 
     * @return A widen function.
     */
    default <Added> WiseTriConsumer<Param1, Param2, Added> append() {
        return (p, q, r) -> accept(p, q);
    }

    /**
     * Widen parameter at first (appended parameter will be ignored).
     * 
     * @return A widen function.
     */
    default <Added> WiseTriConsumer<Added, Param1, Param2> prepend() {
        return (p, q, r) -> accept(q, r);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default WiseConsumer<Param1> assign(Supplier<Param2> param) {
        return p -> accept(p, param.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default WiseConsumer<Param2> preassign(Supplier<Param1> param) {
        return p -> accept(param.get(), p);
    }
}
