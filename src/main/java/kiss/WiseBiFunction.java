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

import java.util.function.BiFunction;

/**
 * @version 2017/05/02 15:27:26
 */
public interface WiseBiFunction<Param1, Param2, Return> extends BiFunction<Param1, Param2, Return> {

    /**
     * <p>
     * Internal API.
     * </p>
     * 
     * @param param1 A proxy parameter.
     * @param param2 A proxy parameter.
     * @return A proxy result.
     * @throws Throwable A sneaky exception for lambda.
     */
    Return APPLY(Param1 param1, Param2 param2) throws Throwable;

    /**
     * {@inheritDoc}
     */
    @Override
    default Return apply(Param1 param1, Param2 param2) {
        try {
            return APPLY(param1, param2);
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
    default WiseFunction<Param2, Return> with(Param1 param1) {
        return param2 -> APPLY(param1, param2);
    }
}