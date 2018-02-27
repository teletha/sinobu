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

import java.util.function.Function;

/**
 * @version 2017/05/02 15:27:31
 */
public interface WiseFunction<Param, Return> extends Function<Param, Return> {

    /**
     * <p>
     * Internal API.
     * </p>
     * 
     * @param param A proxy parameter.
     * @return A proxy result.
     * @throws Throwable A sneaky exception for lambda.
     */
    Return APPLY(Param param) throws Throwable;

    /**
     * {@inheritDoc}
     */
    @Override
    default Return apply(Param param) {
        try {
            return APPLY(param);
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
    default WiseSupplier<Return> with(Param param) {
        return () -> APPLY(param);
    }
}
