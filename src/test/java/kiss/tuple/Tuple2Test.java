/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.tuple;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Ⅱ;
import kiss.Ⅲ;

class Tuple2Test {

    @Test
    void instance() {
        Ⅱ<String, String> value = I.pair("test", "value");
        assert value != null;
        assert value.ⅰ.equals("test");
        assert value.ⅱ.equals("value");
    }

    @Test
    void map() {
        Ⅱ<String, String> value = I.pair("test", "value");
        Ⅱ<String, String> upper = value.map((one, other) -> {
            return I.pair(one.toUpperCase(), other.toUpperCase());
        });
        assert upper != null;
        assert upper.ⅰ.equals("TEST");
        assert upper.ⅱ.equals("VALUE");
    }

    @Test
    void append() {
        Ⅱ<String, String> value = I.pair("test", "value");
        Ⅲ<String, String, String> added = value.ⅲ("add");
        assert added.ⅰ.equals("test");
        assert added.ⅱ.equals("value");
        assert added.ⅲ.equals("add");
    }
}
