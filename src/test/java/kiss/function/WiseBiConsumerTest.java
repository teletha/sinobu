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
import kiss.WiseConsumer;
import kiss.WiseTriConsumer;

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
        concat.bindLazily(null).accept("value");
        assert value.equals("null value");
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
        concat.bindLastLazily(null).accept("value");
        assert value.equals("value null");
    }

    @Test
    void fixHead() {
        concat.fix("fixed").accept("this arg will be ignored", "value");
        assert value.equals("fixed value");
    }

    @Test
    void fixHeadNull() {
        concat.fix(null).accept("this arg will be ignored", "value");
        assert value.equals("null value");
    }

    @Test
    void fixHeadLazily() {
        Variable<String> variable = Variable.of("init");
        WiseBiConsumer<String, String> created = concat.fixLazily(variable);

        created.accept("this arg will be ignored", "value");
        assert value.equals("init value");
        variable.set("change");
        created.accept("this arg will be ignored", "value");
        assert value.equals("change value");
    }

    @Test
    void fixHeadLazilyNull() {
        concat.fixLazily(null).accept("this arg will be ignored", "value");
        assert value.equals("null value");
    }

    @Test
    void fixTail() {
        concat.fixLast("fixed").accept("value", "this arg will be ignored");
        assert value.equals("value fixed");
    }

    @Test
    void fixTailNull() {
        concat.fixLast(null).accept("value", "this arg will be ignored");
        assert value.equals("value null");
    }

    @Test
    void fixTailLazily() {
        Variable<String> variable = Variable.of("init");
        WiseBiConsumer<String, String> created = concat.fixLastLazily(variable);

        created.accept("value", "this arg will be ignored");
        assert value.equals("value init");
        variable.set("change");
        created.accept("value", "this arg will be ignored");
        assert value.equals("value change");
    }

    @Test
    void fixTailLazilyNull() {
        concat.fixLastLazily(null).accept("value", "this arg will be ignored");
        assert value.equals("value null");
    }

    @Test
    void widenHead() {
        WiseTriConsumer<String, String, String> created = concat.head();
        created.accept("ignore", "this is", "used");
        assert value.equals("this is used");
    }

    @Test
    void widenHeadNull() {
        WiseTriConsumer<String, String, String> created = concat.head();
        created.accept(null, "this is", "used");
        assert value.equals("this is used");
    }

    @Test
    void widenTail() {
        WiseTriConsumer<String, String, String> created = concat.tail();
        created.accept("this is", "used", "ignore");
        assert value.equals("this is used");
    }

    @Test
    void widenTailNull() {
        WiseTriConsumer<String, String, String> created = concat.tail();
        created.accept("this is", "used", null);
        assert value.equals("this is used");
    }
}
