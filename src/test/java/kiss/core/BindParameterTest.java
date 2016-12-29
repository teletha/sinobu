/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;

import kiss.I;

/**
 * @version 2016/10/28 14:33:23
 */
public class BindParameterTest {

    @Test
    public void consumer() {
        AtomicInteger value = new AtomicInteger(0);
        Runnable run = I.bind((Consumer<Integer>) e -> value.set(e), 10);
        assert value.get() == 0;
        run.run();
        assert value.get() == 10;
    }

    @Test
    public void consumerNull() {
        Consumer<Integer> non = null;
        Runnable run = I.bind(non, 10);
        assert run != null;
        run.run();
    }

    @Test
    public void biconsumer() {
        AtomicInteger value = new AtomicInteger(0);
        Runnable run = I.bind((BiConsumer<Integer, Integer>) (a, b) -> value.set(a + b), 10, 20);
        assert value.get() == 0;
        run.run();
        assert value.get() == 30;
    }

    @Test
    public void biconsumerNull() {
        BiConsumer<Integer, Integer> non = null;
        Runnable run = I.bind(non, 10, 20);
        assert run != null;
        run.run();
    }

    @Test
    public void function() {
        Supplier<Integer> supply = I.bind(e -> e + 10, 10);
        assert supply.get() == 20;
    }

    @Test(expected = NullPointerException.class)
    public void functionNull() {
        I.bind((Function) null, 10);
    }

    @Test
    public void bifunction() {
        Supplier<Integer> supply = I.bind((a, b) -> a + b, 10, 20);
        assert supply.get() == 30;
    }

    @Test(expected = NullPointerException.class)
    public void bifunctionNull() {
        I.bind((BiFunction) null, 10, 20);
    }
}
