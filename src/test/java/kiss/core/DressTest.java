/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;

import kiss.I;
import kiss.UsefulBiConsumer;
import kiss.UsefulBiFunction;
import kiss.UsefulConsumer;
import kiss.UsefulFunction;
import kiss.UsefulRunnable;
import kiss.UsefulSupplier;

/**
 * @version 2017/05/02 14:36:31
 */
public class DressTest {

    @Test
    public void runnable() throws Exception {
        Runnable lambda = () -> {
        };
        UsefulRunnable dressed = I.dress(lambda);
        assert dressed != lambda;

        lambda = (UsefulRunnable) () -> {
        };
        dressed = I.dress(lambda);
        assert dressed == lambda;
    }

    @Test
    public void consumer() throws Exception {
        Consumer lambda = v -> {
        };
        UsefulConsumer dressed = I.dress(lambda);
        assert dressed != lambda;

        lambda = (UsefulConsumer) v -> {
        };
        dressed = I.dress(lambda);
        assert dressed == lambda;
    }

    @Test
    public void biconsumer() throws Exception {
        BiConsumer lambda = (p1, p2) -> {
        };
        UsefulBiConsumer dressed = I.dress(lambda);
        assert dressed != lambda;

        lambda = (UsefulBiConsumer) (p1, p2) -> {
        };
        dressed = I.dress(lambda);
        assert dressed == lambda;
    }

    @Test
    public void supplier() throws Exception {
        Supplier lambda = () -> "";
        UsefulSupplier dressed = I.dress(lambda);
        assert dressed != lambda;

        lambda = (UsefulSupplier) () -> "";
        dressed = I.dress(lambda);
        assert dressed == lambda;
    }

    @Test
    public void function() throws Exception {
        Function lambda = p -> "";
        UsefulFunction dressed = I.dress(lambda);
        assert dressed != lambda;

        lambda = (UsefulFunction) p -> "";
        dressed = I.dress(lambda);
        assert dressed == lambda;
    }

    @Test
    public void bifunction() throws Exception {
        BiFunction lambda = (p1, p2) -> "";
        UsefulBiFunction dressed = I.dress(lambda);
        assert dressed != lambda;

        lambda = (UsefulBiFunction) (p1, p2) -> "";
        dressed = I.dress(lambda);
        assert dressed == lambda;
    }
}
