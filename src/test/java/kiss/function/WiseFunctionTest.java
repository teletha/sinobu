/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.function;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.Variable;
import kiss.WiseFunction;
import kiss.WiseSupplier;

class WiseFunctionTest {

    WiseFunction<String, String> identity = v -> v;

    @Test
    void narrowHead() {
        assert identity.bind("fixed").get() == "fixed";
    }

    @Test
    void narrowHeadNull() {
        assert identity.bind((String) null).get() == null;
    }

    @Test
    void narrowHeadLazily() {
        Variable<String> variable = Variable.of("init");
        WiseSupplier<String> created = identity.bindLazily(variable);

        assert created.get() == "init";
        variable.set("change");
        assert created.get() == "change";
    }

    @Test
    void narrowHeadLazilyNull() {
        Assertions.assertThrows(NullPointerException.class, () -> identity.bindLazily(null));
    }

    @Test
    void narrowTail() {
        assert identity.bindLast("fixed").get() == "fixed";
    }

    @Test
    void narrowTailNull() {
        assert identity.bindLast((String) null).get() == null;
    }

    @Test
    void narrowTailLazily() {
        Variable<String> variable = Variable.of("init");
        WiseSupplier<String> created = identity.bindLastLazily(variable);

        assert created.get() == "init";
        variable.set("change");
        assert created.get() == "change";
    }

    @Test
    void narrowTailLazilyNull() {
        Assertions.assertThrows(NullPointerException.class, () -> identity.bindLastLazily(null));
    }

    // @Test
    // void memo() {
    // AtomicInteger called = new AtomicInteger();
    //
    // WiseFunction<Integer, Integer> calc = v -> {
    // called.incrementAndGet();
    // return v * 2;
    // };
    // WiseFunction<Integer, Integer> memoize = calc.memoize();
    // assert memoize.apply(10) == 20;
    // assert called.get() == 1;
    // assert memoize.apply(10) == 20;
    // assert called.get() == 1;
    // assert memoize.apply(20) == 40;
    // assert called.get() == 2;
    // }
}