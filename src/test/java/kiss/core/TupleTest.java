/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import kiss.I;
import kiss.Ternary;

import org.junit.Test;

/**
 * @version 2015/04/10 1:36:39
 */
public class TupleTest {

    @Test
    public void binary() throws Exception {
        Ternary<String, Integer, Long> value = I.pair("String", 10, 30L);

        assert value.く == "String";
        assert value.巜 == 10;
        assert value.巛 == 30L;

        value = value.く("Replace");
    }
}
