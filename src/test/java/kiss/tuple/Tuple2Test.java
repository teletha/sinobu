/*
 * Copyright (C) 2018 Nameless Production Committee
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

/**
 * @version 2016/03/27 13:21:47
 */
public class Tuple2Test {

    @Test
    public void instance() {
        Ⅱ<String, String> value = I.pair("test", "value");
        assert value != null;
        assert value.ⅰ.equals("test");
        assert value.ⅱ.equals("value");
    }

    @Test
    public void map() {
        Ⅱ<String, String> value = I.pair("test", "value");
        Ⅱ<String, String> upper = value.map(one -> other -> {
            return I.pair(one.toUpperCase(), other.toUpperCase());
        });
        assert upper != null;
        assert upper.ⅰ.equals("TEST");
        assert upper.ⅱ.equals("VALUE");
    }
}
