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

import java.util.Objects;
import java.util.function.Function;

/**
 * @version 2016/12/10 23:40:32
 */
public interface ThrowableFunction<Param, Return> extends Function<Param, Return> {

    /**
     * Applies this function to the given argument.
     *
     * @param param The function argument.
     * @return The function result.
     * @throws Throwable The execution error.
     */
    Return applyWith(Param param) throws Throwable;

    /**
     * {@inheritDoc}
     */
    @Override
    default Return apply(Param param) {
        try {
            return applyWith(param);
        } catch (Throwable e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    default <V> ThrowableFunction<Param, V> andThen(Function<? super Return, ? extends V> after) {
        Objects.requireNonNull(after);
        return param -> after.apply(applyWith(param));
    }

    /**
     * {@inheritDoc}
     */
    default <V> ThrowableFunction<Param, V> andThen(ThrowableFunction<? super Return, ? extends V> after) {
        Objects.requireNonNull(after);
        return param -> after.applyWith(applyWith(param));
    }

    /**
     * Returns a function that always returns its input argument.
     *
     * @param <T> the type of the input and output objects to the function
     * @return a function that always returns its input argument
     */
    static <T> ThrowableFunction<T, T> identity() {
        return param -> param;
    }
}
