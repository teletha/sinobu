/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.lambda;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import kiss.I;
import kiss.Ⅱ;
import kiss.Ⅲ;

/**
 * @version 2016/03/28 9:12:49
 */
public class Lambda {

    public static <Param1, Param2, Result> Function<Param1, Function<Param2, Result>> curry(BiFunction<Param1, Param2, Result> function) {
        return param1 -> param2 -> function.apply(param1, param2);
    }

    public static <Param1, Param2, Result> Function<Param1, Function<Param2, Result>> curry(Function<Ⅱ<Param1, Param2>, Result> function) {
        return param1 -> param2 -> function.apply(I.pair(param1, param2));
    }

    public static <Param1, Param2, Param3, Result> Function<Param1, Function<Param2, Function<Param3, Result>>> curry3(Function<Ⅲ<Param1, Param2, Param3>, Result> function) {
        return param1 -> param2 -> param3 -> function.apply(I.pair(param1, param2, param3));
    }

    public static <Param1, Param2, Result> BiFunction<Param1, Param2, Result> uncurry(Function<Param1, Function<Param2, Result>> function) {
        return (param1, param2) -> function.apply(param1).apply(param2);
    }

    /**
     * <p>
     * Create recursive function.
     * </p>
     * 
     * @param function A target function to convert.
     * @return A converted recursive function.
     */
    public static <Param, Return> Function<Param, Return> recursive(Function<Function<Param, Return>, Function<Param, Return>> function) {
        Map<Param, Return> memo = new HashMap<>();

        Recursive<Function<Param, Return>> recursive = recursiveFunction -> function.apply(param -> {
            return memo.computeIfAbsent(param, value -> recursiveFunction.apply(recursiveFunction).apply(param));
        });

        return recursive.apply(recursive);
    }

    /**
     * @version 2014/07/20 9:37:18
     */
    private interface Recursive<F> extends Function<Recursive<F>, F> {
    }

    /**
     * <p>
     * Create recursive function.
     * </p>
     * 
     * @param function A target function to convert.
     * @return A converted recursive function.
     */
    public static Runnable recursiveR(Function<Runnable, Runnable> function) {
        Recursive<Runnable> recursive = recursiveFunction -> function.apply(() -> {
            recursiveFunction.apply(recursiveFunction).run();
        });
        return recursive.apply(recursive);
    }
}
