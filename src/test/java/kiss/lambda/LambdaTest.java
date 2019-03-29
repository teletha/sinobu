/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.lambda;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Ⅱ;

/**
 * @version 2018/03/01 20:55:59
 */
public class LambdaTest {

    @Test
    public void pairFunction() {
        Function<Ⅱ<Integer, Integer>, Integer> function = I.pair((a, b) -> a * 10 + b);
        assert function.apply(I.pair(1, 2)) == 12;
    }

    @Test
    public void pairConsumer() {
        AtomicInteger value = new AtomicInteger();
        Consumer<Ⅱ<Integer, Integer>> consumer = I.<Integer, Integer> pair((a, b) -> value.addAndGet(a * 10 + b));
        consumer.accept(I.pair(1, 2));

        assert value.get() == 12;
    }
}
