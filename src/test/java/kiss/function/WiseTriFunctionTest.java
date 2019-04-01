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
import kiss.WiseTriFunction;

class WiseTriFunctionTest {

    WiseTriFunction<String, String, String, String> concat = (p, q, r) -> p + " " + q + " " + r;

    @Test
    void narrowHead() {
        assert concat.hide("fixed").apply("this", "value").equals("fixed this value");
    }

    @Test
    void narrowHeadNull() {
        assert concat.hide((String) null).apply("this", "value").equals("null this value");
    }

    @Test
    void narrowHeadLazily() {
        Variable<String> variable = Variable.of("init");
        WiseBiFunction<String, String, String> created = concat.hideLazy(variable);

        assert created.apply("this", "var").equals("init this var");
        variable.set("change");
        assert created.apply("this", "var").equals("change this var");
    }

    @Test
    void narrowHeadLazilyNull() {
        assert concat.hideLazy((Supplier) null).apply("this", "var").equals("null this var");
    }

    @Test
    void narrowTail() {
        assert concat.hideEnd("fixed").apply("value", "is").equals("value is fixed");
    }

    @Test
    void narrowTailNull() {
        assert concat.hideEnd((String) null).apply("value", "is").equals("value is null");
    }

    @Test
    void narrowTailLazily() {
        Variable<String> variable = Variable.of("init");
        WiseBiFunction<String, String, String> created = concat.hideEndLazy(variable);

        assert created.apply("var", "is").equals("var is init");
        variable.set("changed");
        assert created.apply("var", "is").equals("var is changed");
    }

    @Test
    void narrowTailLazilyNull() {
        assert concat.hideEndLazy((Supplier) null).apply("var", "is").equals("var is null");
    }
}
