/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.WiseBiConsumer;
import kiss.WiseBiFunction;
import kiss.WiseConsumer;
import kiss.WiseFunction;
import kiss.WiseRunnable;
import kiss.WiseSupplier;

/**
 * @version 2017/05/02 14:36:31
 */
public class DressTest {

    @Test
    public void runnable() throws Exception {
        Runnable lambda = () -> {
        };
        WiseRunnable dressed = I.wise(lambda);
        assert dressed != lambda;

        lambda = (WiseRunnable) () -> {
        };
        dressed = I.wise(lambda);
        assert dressed == lambda;
    }

    @Test
    public void consumer() throws Exception {
        Consumer lambda = v -> {
        };
        WiseConsumer dressed = I.wise(lambda);
        assert dressed != lambda;

        lambda = (WiseConsumer) v -> {
        };
        dressed = I.wise(lambda);
        assert dressed == lambda;
    }

    @Test
    public void biconsumer() throws Exception {
        BiConsumer lambda = (p1, p2) -> {
        };
        WiseBiConsumer dressed = I.wise(lambda);
        assert dressed != lambda;

        lambda = (WiseBiConsumer) (p1, p2) -> {
        };
        dressed = I.wise(lambda);
        assert dressed == lambda;
    }

    @Test
    public void supplier() throws Exception {
        Supplier lambda = () -> "";
        WiseSupplier dressed = I.wise(lambda);
        assert dressed != lambda;

        lambda = (WiseSupplier) () -> "";
        dressed = I.wise(lambda);
        assert dressed == lambda;
    }

    @Test
    public void function() throws Exception {
        Function lambda = p -> "";
        WiseFunction dressed = I.wise(lambda);
        assert dressed != lambda;

        lambda = (WiseFunction) p -> "";
        dressed = I.wise(lambda);
        assert dressed == lambda;
    }

    @Test
    public void bifunction() throws Exception {
        BiFunction lambda = (p1, p2) -> "";
        WiseBiFunction dressed = I.wise(lambda);
        assert dressed != lambda;

        lambda = (WiseBiFunction) (p1, p2) -> "";
        dressed = I.wise(lambda);
        assert dressed == lambda;
    }
}
