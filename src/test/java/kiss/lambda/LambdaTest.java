/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.lambda;

import kiss.I;
import kiss.Ⅱ;
import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static kiss.lambda.Lambda.recursive;

/**
 * @version 2016/04/04 19:28:05
 */
public class LambdaTest {

    @Test
    public void pairFunction() throws Exception {
        Function<Ⅱ<Integer, Integer>, Integer> function = I.pair((a, b) -> a * 10 + b);
        assert function.apply(I.pair(1, 2)) == 12;
    }

    @Test
    public void pairConsumer() throws Exception {
        AtomicInteger value = new AtomicInteger();
        Consumer<Ⅱ<Integer, Integer>> consumer = I.<Integer, Integer>pair((a, b) -> value.addAndGet(a * 10 + b));
        consumer.accept(I.pair(1, 2));

        assert value.get() == 12;
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
