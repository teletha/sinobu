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

import org.junit.jupiter.api.Test;

import kiss.Variable;
import kiss.WiseBiConsumer;
import kiss.WiseTriConsumer;

class WiseTriConsumerTest {

    String value = null;

    WiseTriConsumer<String, String, String> concat = (p, q, r) -> value = p + " " + q + " " + r;

    @Test
    void narrowHead() {
        concat.preassign("fix").accept("this", "value");
        assert value.equals("fix this value");
    }

    @Test
    void narrowHeadNull() {
        concat.preassign(null).accept("this", "value");
        assert value.equals("null this value");
    }

    @Test
    void narrowHeadLazily() {
        Variable<String> variable = Variable.of("init");
        WiseBiConsumer<String, String> created = concat.preassignLazy(variable);

        created.accept("this", "value");
        assert value.equals("init this value");
        variable.set("change");
        created.accept("this", "value");
        assert value.equals("change this value");
    }

    @Test
    void narrowHeadLazilyNull() {
        concat.preassignLazy(null).accept("this", "value");
        assert value.equals("null this value");
    }

    @Test
    void narrowTail() {
        concat.assign("fixed").accept("value", "is");
        assert value.equals("value is fixed");
    }

    @Test
    void narrowTailNull() {
        concat.assign(null).accept("value", "is");
        assert value.equals("value is null");
    }

    @Test
    void narrowTailLazily() {
        Variable<String> variable = Variable.of("init");
        WiseBiConsumer<String, String> created = concat.assignLazy(variable);

        created.accept("value", "is");
        assert value.equals("value is init");
        variable.set("change");
        created.accept("value", "is");
        assert value.equals("value is change");
    }

    @Test
    void narrowTailLazilyNull() {
        concat.assignLazy(null).accept("value", "is");
        assert value.equals("value is null");
    }
}