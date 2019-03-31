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

import java.util.function.Function;

/**
 * @version 2018/12/07 16:09:15
 */
public interface WiseFunction<Param, Return>
        extends Function<Param, Return>, Narrow<WiseSupplier<Return>, Param, WiseSupplier<Return>, Param> {

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
     * Widen parameter at last (appended parameter will be ignored).
     * 
     * @return A widen function.
     */
    default <Added> WiseBiFunction<Param, Added, Return> append() {
        return (p, q) -> apply(p);
    }

    /**
     * Widen parameter at first (appended parameter will be ignored).
     * 
     * @return A widen function.
     */
    default <Added> WiseBiFunction<Added, Param, Return> prepend() {
        return (p, q) -> apply(q);
    }
}
