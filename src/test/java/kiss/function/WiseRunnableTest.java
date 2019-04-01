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

import kiss.WiseConsumer;
import kiss.WiseRunnable;

class WiseRunnableTest {

    private String value = null;

    WiseRunnable constant = () -> value = "constant";

    @Test
    void widenHead() {
        WiseConsumer<String> created = constant.prepend();
        assert value == null;
        created.accept("ignore");
        assert value.equals("constant");
    }

    @Test
    void widenHeadNull() {
        WiseConsumer<String> created = constant.prepend();
        assert value == null;
        created.accept(null);
        assert value.equals("constant");
    }

    @Test
    void widenTail() {
        WiseConsumer<String> created = constant.append();
        assert value == null;
        created.accept("ignore");
        assert value.equals("constant");
    }

    @Test
    void widenTailNull() {
        WiseConsumer<String> created = constant.append();
        assert value == null;
        created.accept(null);
        assert value.equals("constant");
    }
}
