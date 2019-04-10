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

import kiss.WiseRunnable;

class WiseRunnableTest {

    String value = null;

    WiseRunnable constant = () -> value = "constant";

    @Test
    void invoke() {
        constant.invoke("parameter", "will", "be", "ignored");
        assert value.equals("constant");
    }
}
