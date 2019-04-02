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
        concat.bind("fix").accept("this", "value");
        assert value.equals("fix this value");
    }

    @Test
    void narrowHeadNull() {
        concat.bind(null).accept("this", "value");
        assert value.equals("null this value");
    }

    @Test
    void narrowHeadLazily() {
        Variable<String> variable = Variable.of("init");
        WiseBiConsumer<String, String> created = concat.bindLazily(variable);

        created.accept("this", "value");
        assert value.equals("init this value");
        variable.set("change");
        created.accept("this", "value");
        assert value.equals("change this value");
    }

    @Test
    void narrowHeadLazilyNull() {
        concat.bindLazily(null).accept("this", "value");
        assert value.equals("null this value");
    }

    @Test
    void narrowTail() {
        concat.bindLast("fixed").accept("value", "is");
        assert value.equals("value is fixed");
    }

    @Test
    void narrowTailNull() {
        concat.bindLast(null).accept("value", "is");
        assert value.equals("value is null");
    }

    @Test
    void narrowTailLazily() {
        Variable<String> variable = Variable.of("init");
        WiseBiConsumer<String, String> created = concat.bindLastLazily(variable);

        created.accept("value", "is");
        assert value.equals("value is init");
        variable.set("change");
        created.accept("value", "is");
        assert value.equals("value is change");
    }

    @Test
    void narrowTailLazilyNull() {
        concat.bindLastLazily(null).accept("value", "is");
        assert value.equals("value is null");
    }

    @Test
    void fixHead() {
        concat.fix("fixed").accept("this arg will be ignored", "this", "value");
        assert value.equals("fixed this value");
    }

    @Test
    void fixHeadNull() {
        concat.fix(null).accept("this arg will be ignored", "is", "accepted");
        assert value.equals("null is accepted");
    }

    @Test
    void fixHeadLazily() {
        Variable<String> variable = Variable.of("current value");
        WiseTriConsumer<String, String, String> created = concat.fixLazily(variable);

        created.accept("this arg will be ignored", "is", "used");
        assert value.equals("current value is used");
        variable.set("modified value");
        created.accept("this arg will be ignored", "is", "used");
        assert value.equals("modified value is used");
    }

    @Test
    void fixHeadLazilyNull() {
        concat.fixLazily(null).accept("this arg will be ignored", "is", "acceptable");
        assert value.equals("null is acceptable");
    }

    @Test
    void fixTail() {
        concat.fixLast("fixed").accept("value", "is", "this arg will be ignored");
        assert value.equals("value is fixed");
    }

    @Test
    void fixTailNull() {
        concat.fixLast(null).accept("it", "accepts", "this arg will be ignored");
        assert value.equals("it accepts null");
    }

    @Test
    void fixTailLazily() {
        Variable<String> variable = Variable.of("init");
        WiseTriConsumer<String, String, String> created = concat.fixLastLazily(variable);

        created.accept("value", "is", "this arg will be ignored");
        assert value.equals("value is init");
        variable.set("modified");
        created.accept("value", "is", "this arg will be ignored");
        assert value.equals("value is modified");
    }

    @Test
    void fixTailLazilyNull() {
        concat.fixLastLazily(null).accept("it", "accepts", "this arg will be ignored");
        assert value.equals("it accepts null");
    }
}
