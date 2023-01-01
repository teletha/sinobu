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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import kiss.Variable;
import kiss.WiseBiConsumer;
import kiss.WiseConsumer;

class WiseBiConsumerTest {

    String value = null;

    WiseBiConsumer<String, String> concat = (p, q) -> value = p + " " + q;

    @Test
    void narrowHead() {
        concat.bind("fixed").accept("value");
        assert value.equals("fixed value");
    }

    @Test
    void narrowHeadNull() {
        concat.bind(null).accept("value");
        assert value.equals("null value");
    }

    @Test
    void narrowHeadLazily() {
        Variable<String> variable = Variable.of("init");
        WiseConsumer<String> created = concat.bindLazily(variable);

        created.accept("value");
        assert value.equals("init value");
        variable.set("change");
        created.accept("value");
        assert value.equals("change value");
    }

    @Test
    void narrowHeadLazilyNull() {
        Assertions.assertThrows(NullPointerException.class, () -> concat.bindLazily(null));
    }

    @Test
    void narrowTail() {
        concat.bindLast("fixed").accept("value");
        assert value.equals("value fixed");
    }

    @Test
    void narrowTailNull() {
        concat.bindLast(null).accept("value");
        assert value.equals("value null");
    }

    @Test
    void narrowTailLazily() {
        Variable<String> variable = Variable.of("init");
        WiseConsumer<String> created = concat.bindLastLazily(variable);

        created.accept("value");
        assert value.equals("value init");
        variable.set("change");
        created.accept("value");
        assert value.equals("value change");
    }

    @Test
    void narrowTailLazilyNull() {
        Assertions.assertThrows(NullPointerException.class, () -> concat.bindLastLazily(null));
    }
}