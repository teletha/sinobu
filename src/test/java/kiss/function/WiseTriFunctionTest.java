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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.Variable;
import kiss.WiseBiFunction;
import kiss.WiseTriFunction;

class WiseTriFunctionTest {

    WiseTriFunction<String, String, String, String> concat = (p, q, r) -> p + " " + q + " " + r;

    @Test
    void narrowHead() {
        assert concat.bind("fixed").apply("this", "value").equals("fixed this value");
    }

    @Test
    void narrowHeadNull() {
        assert concat.bind((String) null).apply("this", "value").equals("null this value");
    }

    @Test
    void narrowHeadLazily() {
        Variable<String> variable = Variable.of("init");
        WiseBiFunction<String, String, String> created = concat.bindLazily(variable);

        assert created.apply("this", "var").equals("init this var");
        variable.set("change");
        assert created.apply("this", "var").equals("change this var");
    }

    @Test
    void narrowHeadLazilyNull() {
        Assertions.assertThrows(NullPointerException.class, () -> concat.bindLazily(null));
    }

    @Test
    void narrowTail() {
        assert concat.bindLast("fixed").apply("value", "is").equals("value is fixed");
    }

    @Test
    void narrowTailNull() {
        assert concat.bindLast((String) null).apply("value", "is").equals("value is null");
    }

    @Test
    void narrowTailLazily() {
        Variable<String> variable = Variable.of("init");
        WiseBiFunction<String, String, String> created = concat.bindLastLazily(variable);

        assert created.apply("var", "is").equals("var is init");
        variable.set("changed");
        assert created.apply("var", "is").equals("var is changed");
    }

    @Test
    void narrowTailLazilyNull() {
        Assertions.assertThrows(NullPointerException.class, () -> concat.bindLastLazily(null));
    }
}