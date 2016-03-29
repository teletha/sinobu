/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.lambda;

import static kiss.lambda.Lambda.*;

import java.math.BigInteger;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.junit.Test;

import kiss.Ⅲ;
import lombok.experimental.ExtensionMethod;

/**
 * @version 2014/07/20 3:33:24
 */
@ExtensionMethod({Lambda.class})
public class LambdaTest {

    @Test
    public void lambda() throws Exception {
        BiFunction<String, String, String> function = (a, b) -> a + b;
        Function<String, Function<String, String>> curried = function.curry();
        BiFunction<String, String, String> uncurried = curried.uncurry();

        System.out.println(curried.apply("test").apply("value"));
        System.out.println(uncurried.apply("test", "value"));

        Function<Ⅲ<String, String, String>, String> f = a -> a.ⅰ + a.ⅱ + a.ⅲ;
        Function<String, Function<String, Function<String, String>>> curry3 = f.curry3();
        System.out.println(curry3.apply("A").apply("B").apply("C"));

        Function<Function<String, String>, Function<String, String>> aa = ff -> v -> {
            return v;
        };

        Function<String, String> recursive = aa.recursive();
    }

    @Test
    public void testname() throws Exception {
        Function<BigInteger, BigInteger> fib = recursive(f -> n -> {
            if (n.intValue() <= 2) return BigInteger.ONE;
            return f.apply(n.subtract(BigInteger.ONE)).add(f.apply(n.subtract(BigInteger.valueOf(2))));
        });
        assert fib.apply(BigInteger.valueOf(10)).intValue() == 55;
    }
}
