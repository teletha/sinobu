/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.function;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.WiseConsumer;
import kiss.WiseFunction;
import kiss.WiseRunnable;
import kiss.WiseSupplier;

class WiseTest {

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
}