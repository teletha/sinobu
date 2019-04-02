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
import kiss.WiseTriFunction;

class WiseBiFunctionTest {

    WiseBiFunction<String, String, String> concat = (p, q) -> p + " " + q;

    @Test
    void narrowHead() {
        assert concat.bind("fixed").apply("value").equals("fixed value");
    }

    @Test
    void narrowHeadNull() {
        assert concat.bind((String) null).apply("value").equals("null value");
    }

    @Test
    void narrowHeadLazily() {
        Variable<String> variable = Variable.of("init");
        WiseFunction<String, String> created = concat.bindLazily(variable);

        assert created.apply("var").equals("init var");
        variable.set("change");
        assert created.apply("var").equals("change var");
    }

    @Test
    void narrowHeadLazilyNull() {
        assert concat.bindLazily((Supplier) null).apply("var").equals("null var");
    }

    @Test
    void narrowTail() {
        assert concat.bindLast("fixed").apply("value").equals("value fixed");
    }

    @Test
    void narrowTailNull() {
        assert concat.bindLast((String) null).apply("value").equals("value null");
    }

    @Test
    void narrowTailLazily() {
        Variable<String> variable = Variable.of("init");
        WiseFunction<String, String> created = concat.bindLastLazily(variable);

        assert created.apply("var").equals("var init");
        variable.set("change");
        assert created.apply("var").equals("var change");
    }

    @Test
    void narrowTailLazilyNull() {
        assert concat.bindLastLazily((Supplier) null).apply("var").equals("var null");
    }

    @Test
    void widenHead() {
        WiseTriFunction<String, String, String, String> created = concat.head();
        assert created.apply("ignore", "use", "value").equals("use value");
        assert created.apply(null, "use", "value").equals("use value");
        assert created.apply(null, null, null).equals("null null");
    }

    @Test
    void widenTail() {
        WiseTriFunction<String, String, String, String> created = concat.tail();
        assert created.apply("use", "value", "ignore").equals("use value");
        assert created.apply("use", "value", null).equals("use value");
        assert created.apply(null, null, null).equals("null null");
    }
}
