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
}
