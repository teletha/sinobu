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

import kiss.WiseFunction;
import kiss.WiseSupplier;

class WiseSupplierTest {

    WiseSupplier<String> constant = () -> "constant";

    @Test
    void widenHead() {
        WiseFunction<String, String> created = constant.up();
        assert created.apply("ignore").equals("constant");
        assert created.apply(null).equals("constant");
    }

    @Test
    void widenTail() {
        WiseFunction<String, String> created = constant.as();
        assert created.apply("ignore").equals("constant");
        assert created.apply(null).equals("constant");
    }
}
