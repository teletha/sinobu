/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss;

import java.util.function.Consumer;

public interface WiseConsumer<Param> extends Consumer<Param>, Narrow<WiseRunnable, Param, WiseRunnable, Param> {

    /**
     * Internal API.
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
     * {@inheritDoc}
     */
    @Override
    default Param invoke(Object... params) {
        accept((Param) params[0]);
        return (Param) params[0];
    }
}