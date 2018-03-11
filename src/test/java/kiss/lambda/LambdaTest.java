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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;

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

    @Test
    public void recursiveRunnable() {
        AtomicInteger value = new AtomicInteger();
        Runnable function = I.recurseR(self -> () -> {
            if (value.get() < 10) {
                value.incrementAndGet();
                self.run();
            }
        });
        function.run();

        assert value.get() == 10;
    }

    @Test
    public void recursiveConsumer() {
        AtomicInteger value = new AtomicInteger();
        Consumer<Integer> function = I.recurseC(self -> p -> {
            value.set(p);

            if (p < 10) {
                self.accept(p + 1);
            }
        });
        function.accept(0);

        assert value.get() == 10;
    }

    @Test
    public void recursiveSupplier() {
        AtomicInteger value = new AtomicInteger();
        Supplier<Integer> function = I.recurseS(self -> () -> {
            if (value.get() < 10) {
                value.incrementAndGet();
                return self.get();
            } else {
                return value.get();
            }
        });

        assert function.get() == 10;
    }

    @Test
    public void recursiveFunction() {
        Function<Integer, Integer> function = I.recurseF(self -> param -> {
            if (param < 10) {
                return self.apply(param + 1);
            } else {
                return param;
            }
        });

        assert function.apply(0) == 10;
    }

    @Test
    public void recursiveBiFunction() {
        BiFunction<Integer, Integer, Integer> function = I.recurseBF(self -> (param1, param2) -> {
            if (param1 < 10) {
                return self.apply(param1 + 1, param2 + 2);
            } else {
                return param2;
            }
        });

        assert function.apply(0, 10) == 30;
    }
}
