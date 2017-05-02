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

import java.util.function.BiFunction;

/**
 * @version 2017/05/02 15:27:26
 */
public interface WiseBiFunction<P1, P2, Return> extends BiFunction<P1, P2, Return> {

    /**
     * <p>
     * Internal API.
     * </p>
     * 
     * @param p1 A proxy parameter.
     * @param p2 A proxy parameter.
     * @return A proxy result.
     * @throws Throwable A sneaky exception for lambda.
     */
    Return APPLY(P1 p1, P2 p2) throws Throwable;

    /**
     * {@inheritDoc}
     */
    @Override
    default Return apply(P1 p1, P2 p2) {
        try {
            return APPLY(p1, p2);
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
    default WiseFunction<P2, Return> with(P1 p1) {
        return p2 -> APPLY(p1, p2);
    }
}
