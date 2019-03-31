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

import java.util.function.Consumer;

/**
 * @version 2018/04/02 8:35:45
 */
public interface WiseConsumer<Param> extends Consumer<Param>, Narrow<WiseRunnable, Param, WiseRunnable, Param> {

    /**
     * <p>
     * Internal API.
     * </p>
     * 
     * @param param A proxy parameter.
     * @throws Throwable A sneaky exception for lambda.
     */
    void ACCEPT(Param param) throws Throwable;

    /**
     * {@inheritDoc}
     */
    @Override
    default void accept(Param param) {
        try {
            ACCEPT(param);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * Widen parameter at last (appended parameter will be ignored).
     * 
     * @return A widen function.
     */
    default <Added> WiseBiConsumer<Param, Added> append() {
        return (p, q) -> accept(p);
    }

    /**
     * Widen parameter at first (appended parameter will be ignored).
     * 
     * @return A widen function.
     */
    default <Added> WiseBiConsumer<Added, Param> prepend() {
        return (p, q) -> accept(q);
    }
}
