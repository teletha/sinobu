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

import java.util.function.Function;

/**
 * @version 2017/02/02 12:18:34
 */
public interface ThrowableFunction<Param, Return> extends Function<Param, Return>, EnhancedLambda {

    /**
     * Applies this function to the given argument.
     *
     * @param param The function argument.
     * @return The function result.
     * @throws Throwable The execution error.
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
}
