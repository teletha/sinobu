/*
 * Copyright (C) 2022 The SINOBU Development Team
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
import kiss.WiseConsumer;
import kiss.WiseRunnable;

class WiseConsumerTest {

    String value = null;

    WiseConsumer<String> setter = v -> value = v;

    @Test
    void narrowHead() {
        setter.bind("fixed").run();
        assert value.equals("fixed");
    }

    @Test
    void narrowHeadNull() {
        setter.bind(null).run();
        assert value == null;
    }

    @Test
    void narrowHeadLazily() {
        Variable<String> variable = Variable.of("init");
        WiseRunnable created = setter.bindLazily(variable);

        created.run();
        assert value.equals("init");
        variable.set("change");
        created.run();
        assert value.equals("change");
    }

    @Test
    void narrowHeadLazilyNull() {
        Assertions.assertThrows(NullPointerException.class, () -> setter.bindLazily(null));
    }

    @Test
    void narrowTail() {
        setter.bindLast("fixed").run();
        assert value.equals("fixed");
    }

    @Test
    void narrowTailNull() {
        setter.bindLast(null).run();
        assert value == null;
    }

    @Test
    void narrowTailLazily() {
        Variable<String> variable = Variable.of("init");
        WiseRunnable created = setter.bindLastLazily(variable);

        created.run();
        assert value.equals("init");
        variable.set("change");
        created.run();
        assert value.equals("change");
    }

    @Test
    void narrowTailLazilyNull() {
        Assertions.assertThrows(NullPointerException.class, () -> setter.bindLastLazily(null));
    }
}