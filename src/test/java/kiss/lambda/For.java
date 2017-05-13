/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.lambda;

import java.util.function.Function;

import org.junit.Test;

import kiss.Variable;

/**
 * @version 2017/05/13 15:30:14
 */
public class For {

    @Test
    public void testname() throws Exception {
        Variable<Integer> p1 = Variable.of(10);
        Variable<Integer> p2 = Variable.of(20);

        Sugar<Integer> yield = yield(p1, p2, v1 -> v2 -> v1 + v2);
        System.out.println(yield);
    }

    public static <R, P> R yield(Variable<P> param1, Function<P, R> mapper) {
        return param1.map(mapper).get();
    }

    public static <S extends Sugar, R, P1, P2> Sugar<R> yield(Sugar<P1> p1, Sugar<P2> p2, Function<P1, Function<P2, R>> mapper) {
        return p1.convertFlat(v1 -> p2.convert(v2 -> mapper.apply(v1).apply(v2)));
    }

    public static interface Sugar<T> {
        <R> Sugar<R> convert(Function<T, R> mapper);

        <R> Sugar<R> convertFlat(Function<T, Sugar<R>> mapper);
    }
}
