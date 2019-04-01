/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.function;

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
 * @version 2018/07/22 20:06:49
 */
class DressTest {

    @Test
    void runnable() {
        Runnable lambda = () -> {
        };
        WiseRunnable dressed = I.wiseR(lambda);
        assert dressed != lambda;

        lambda = (WiseRunnable) () -> {
        };
        dressed = I.wiseR(lambda);
        assert dressed == lambda;

        lambda = null;
        dressed = I.wiseR(lambda);
        assert dressed != lambda;
        dressed.run();
    }

    @Test
    void consumer() {
        Consumer lambda = v -> {
        };
        WiseConsumer dressed = I.wiseC(lambda);
        assert dressed != lambda;

        lambda = (WiseConsumer) v -> {
        };
        dressed = I.wiseC(lambda);
        assert dressed == lambda;
    }

    @Test
    void biconsumer() {
        BiConsumer lambda = (p1, p2) -> {
        };
        WiseBiConsumer dressed = I.wiseBC(lambda);
        assert dressed != lambda;

        lambda = (WiseBiConsumer) (p1, p2) -> {
        };
        dressed = I.wiseBC(lambda);
        assert dressed == lambda;
    }

    @Test
    void supplier() {
        Supplier lambda = () -> "";
        WiseSupplier dressed = I.wiseS(lambda);
        assert dressed != lambda;

        lambda = (WiseSupplier) () -> "";
        dressed = I.wiseS(lambda);
        assert dressed == lambda;
    }

    @Test
    void function() {
        Function lambda = p -> "";
        WiseFunction dressed = I.wiseF(lambda);
        assert dressed != lambda;

        lambda = (WiseFunction) p -> "";
        dressed = I.wiseF(lambda);
        assert dressed == lambda;
    }

    @Test
    void bifunction() {
        BiFunction lambda = (p1, p2) -> "";
        WiseBiFunction dressed = I.wiseBF(lambda);
        assert dressed != lambda;

        lambda = (WiseBiFunction) (p1, p2) -> "";
        dressed = I.wiseBF(lambda);
        assert dressed == lambda;
    }
}
