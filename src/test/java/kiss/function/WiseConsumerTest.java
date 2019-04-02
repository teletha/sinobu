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
        setter.bindLazily(null).run();
        assert value == null;
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
        setter.bindLastLazily(null).run();
        assert value == null;
    }

    @Test
    void fixHead() {
        setter.fix("fixed").accept("this arg will be ignored");
        assert value.equals("fixed");
    }

    @Test
    void fixHeadNull() {
        setter.fix(null).accept("this arg will be ignored");
        assert value == null;
    }

    @Test
    void fixHeadLazily() {
        Variable<String> variable = Variable.of("init");
        WiseConsumer<String> created = setter.fixLazily(variable);

        created.accept("this arg will be ignored");
        assert value.equals("init");
        variable.set("change");
        created.accept("this arg will be ignored");
        assert value.equals("change");
    }

    @Test
    void fixHeadLazilyNull() {
        setter.fixLazily(null).accept("this arg will be ignored");
        assert value == null;
    }

    @Test
    void fixTail() {
        setter.fixLast("fixed").accept("this arg will be ignored");
        assert value.equals("fixed");
    }

    @Test
    void fixTailNull() {
        setter.fixLast(null).accept("this arg will be ignored");
        assert value == null;
    }

    @Test
    void fixTailLazily() {
        Variable<String> variable = Variable.of("init");
        WiseConsumer<String> created = setter.fixLastLazily(variable);

        created.accept("this arg will be ignored");
        assert value.equals("init");
        variable.set("change");
        created.accept("this arg will be ignored");
        assert value.equals("change");
    }

    @Test
    void fixTailLazilyNull() {
        setter.fixLastLazily(null).accept("this arg will be ignored");
        assert value == null;
    }

    @Test
    void widenHead() {
        WiseBiConsumer<String, String> created = setter.head();
        created.accept("ignore", "use");
        assert value.equals("use");
    }

    @Test
    void widenHeadNull() {
        WiseBiConsumer<String, String> created = setter.head();
        created.accept(null, null);
        assert value == null;
    }

    @Test
    void widenTail() {
        WiseBiConsumer<String, String> created = setter.tail();
        created.accept("use", "ignore");
        assert value.equals("use");
    }

    @Test
    void widenTailNull() {
        WiseBiConsumer<String, String> created = setter.tail();
        created.accept(null, null);
        assert value == null;
    }
}
