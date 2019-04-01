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
        assert identity.preassign("fixed").get() == "fixed";
    }

    @Test
    void narrowHeadNull() {
        assert identity.preassign((String) null).get() == null;
    }

    @Test
    void narrowHeadLazily() {
        Variable<String> variable = Variable.of("init");
        WiseSupplier<String> created = identity.preassignLazy(variable);

        assert created.get() == "init";
        variable.set("change");
        assert created.get() == "change";
    }

    @Test
    void narrowHeadLazilyNull() {
        assert identity.preassignLazy((Supplier) null).get() == null;
    }

    @Test
    void narrowTail() {
        assert identity.assign("fixed").get() == "fixed";
    }

    @Test
    void narrowTailNull() {
        assert identity.assign((String) null).get() == null;
    }

    @Test
    void narrowTailLazily() {
        Variable<String> variable = Variable.of("init");
        WiseSupplier<String> created = identity.assignLazy(variable);

        assert created.get() == "init";
        variable.set("change");
        assert created.get() == "change";
    }

    @Test
    void narrowTailLazilyNull() {
        assert identity.assignLazy((Supplier) null).get() == null;
    }

    @Test
    void widenHead() {
        WiseBiFunction<String, String, String> created = identity.prepend();
        assert created.apply("ignore", "use") == "use";
        assert created.apply(null, "use") == "use";
        assert created.apply(null, null) == null;
    }

    @Test
    void widenTail() {
        WiseBiFunction<String, String, String> created = identity.append();
        assert created.apply("use", "ignore") == "use";
        assert created.apply("use", null) == "use";
        assert created.apply(null, null) == null;
    }
}
