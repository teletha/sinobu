/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.function;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import kiss.Variable;
import kiss.WiseBiFunction;
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
        assert identity.bindLazily((Supplier) null).get() == null;
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
        assert identity.bindLastLazily((Supplier) null).get() == null;
    }

    @Test
    void widenHead() {
        WiseBiFunction<String, String, String> created = identity.up();
        assert created.apply("ignore", "use") == "use";
        assert created.apply(null, "use") == "use";
        assert created.apply(null, null) == null;
    }

    @Test
    void widenTail() {
        WiseBiFunction<String, String, String> created = identity.as();
        assert created.apply("use", "ignore") == "use";
        assert created.apply("use", null) == "use";
        assert created.apply(null, null) == null;
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
