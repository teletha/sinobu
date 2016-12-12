/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.util.function.Function;

/**
 * @version 2016/12/12 9:28:01
 */
public interface ThrowableFunction<Param, Return> extends Function<Param, Return> {

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
