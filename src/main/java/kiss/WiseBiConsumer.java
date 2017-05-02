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
 * @version 2017/05/02 15:27:50
 */
public interface WiseBiConsumer<P1, P2> extends BiConsumer<P1, P2> {

    /**
     * <p>
     * Internal API.
     * </p>
     * 
     * @param p1 A proxy parameter.
     * @param p2 A proxy parameter.
     * @throws Throwable A sneaky exception for lambda.
     */
    void ACCEPT(P1 p1, P2 p2) throws Throwable;

    /**
     * {@inheritDoc}
     */
    @Override
    default void accept(P1 p1, P2 p2) {
        try {
            ACCEPT(p1, p2);
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
     * @param p1 A fixed parameter.
     * @return A partial applied function.
     */
    default WiseConsumer<P2> with(P1 p1) {
        return p2 -> ACCEPT(p1, p2);
    }
}
