/*
 * Copyright (C) 2023 The SINOBU Development Team
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.WiseConsumer;
import kiss.WiseFunction;
import kiss.WiseRunnable;
import kiss.WiseSupplier;

class WiseTest {

    /**
     * @see I#wiseR(Runnable)
     */
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

    /**
     * @see I#wiseC(Consumer)
     */
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
    void consumerWithNull() {
        Assertions.assertThrows(NullPointerException.class, () -> I.wiseC((Consumer) null));
    }

    /**
     * @see I#wiseC(Runnable)
     */
    @Test
    void consumerFromRunnable() throws Throwable {
        int[] counter = {0};
        WiseConsumer<String> consumer = I.wiseC(() -> counter[0]++);
        assert counter[0] == 0;

        consumer.accept("invoke as Consumer");
        assert counter[0] == 1;

        consumer.ACCEPT("invoke as WiseConsumer");
        assert counter[0] == 2;
    }

    @Test
    void consumerFromNullRunnable() {
        Assertions.assertThrows(NullPointerException.class, () -> I.wiseC((Runnable) null));
    }

    /**
     * @see I#wiseS(Supplier)
     */
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
    void supplierWithNull() {
        Assertions.assertThrows(NullPointerException.class, () -> I.wiseS((Supplier) null));
    }

    /**
     * @see I#wiseS(Object)
     */
    @Test
    void supplierFromConstant() {
        WiseSupplier supplier = I.wiseS("constant");
        assert supplier.get().equals("constant");
    }

    /**
     * @see I#wiseS(Object)
     */
    @Test
    void supplierFromNullConstant() {
        WiseSupplier supplier = I.wiseS((Object) null);
        assert supplier.get() == null;
    }

    /**
     * @see I#wiseF(Function)
     */
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
    void functionWithNull() {
        Assertions.assertThrows(NullPointerException.class, () -> I.wiseF((Function) null));
    }

    /**
     * @see I#wiseF(Supplier)
     */
    @Test
    void functionFromSupplier() {
        WiseFunction<String, String> function = I.wiseF(() -> "constant");
        assert function.apply("any value").equals("constant");
        assert function.apply("returns the fixed value").equals("constant");
    }

    @Test
    void functionFromNullSupplier() {
        Assertions.assertThrows(NullPointerException.class, () -> I.wiseF((Supplier) null));
    }

    /**
     * @see I#wiseF(Object)
     */
    @Test
    void functionFromConstant() {
        WiseFunction<String, String> function = I.wiseF("constant");
        assert function.apply("any value").equals("constant");
        assert function.apply("returns the fixed value").equals("constant");
    }

    /**
     * @see I#wiseF(Object)
     */
    @Test
    void functionFromNullConstant() {
        WiseFunction<String, String> function = I.wiseF((String) null);
        assert function.apply("any value") == null;
        assert function.apply("returns the fixed value") == null;
    }
}