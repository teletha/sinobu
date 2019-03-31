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

import java.util.function.Supplier;

/**
 * @version 2018/04/02 8:34:59
 */
public interface WiseTriFunction<Param1, Param2, Param3, Return>
        extends Narrow<WiseBiFunction<Param1, Param2, Return>, Param3, WiseBiFunction<Param2, Param3, Return>, Param1> {

    /**
     * <p>
     * Internal API.
     * </p>
     * 
     * @param param1 A proxy parameter.
     * @param param2 A proxy parameter.
     * @param param3 A proxy parameter.
     * @return A proxy result.
     * @throws Throwable A sneaky exception for lambda.
     */
    Return APPLY(Param1 param1, Param2 param2, Param3 param3) throws Throwable;

    /**
     * Applies this function to the given argument.
     *
     * @param param1 The function argument.
     * @param param2 The function argument.
     * @param param3 The function argument.
     * @return The function result.
     */
    default Return apply(Param1 param1, Param2 param2, Param3 param3) {
        try {
            return APPLY(param1, param2, param3);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default WiseBiFunction<Param1, Param2, Return> assign(Supplier<Param3> param) {
        return (p, q) -> apply(p, q, param.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default WiseBiFunction<Param2, Param3, Return> preassign(Supplier<Param1> param) {
        return (p, q) -> apply(param.get(), p, q);
    }
}
