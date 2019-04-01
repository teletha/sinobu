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

import java.util.function.BiFunction;

/**
 * @version 2018/04/02 8:35:58
 */
public interface WiseBiFunction<Param1, Param2, Return>
        extends BiFunction<Param1, Param2, Return>,
        Narrow<WiseFunction<Param1, Return>, Param2, WiseFunction<Param2, Return>, Param1, WiseBiFunction<Param1, Param2, Return>>,
        Widen<WiseTriFunction, WiseBiFunction<Param1, Param2, Return>> {

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
     * {@inheritDoc}
     */
    @Override
    default Return invoke(Object... params) {
        return apply((Param1) params[0], (Param2) params[1]);
    }
}
