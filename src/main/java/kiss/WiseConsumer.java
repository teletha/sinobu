/*
 * Copyright (C) 2018 Nameless Production Committee
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
 * @version 2017/05/02 14:33:18
 */
public interface WiseConsumer<Param> extends Consumer<Param> {

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
     * <p>
     * Apply parameter partialy.
     * </p>
     * 
     * @param function A target function to apply parameter.
     * @param param A fixed parameter.
     * @return A partial applied function.
     */
    default WiseRunnable with(Param param) {
        return () -> ACCEPT(param);
    }
}