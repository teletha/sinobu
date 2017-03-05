/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import org.junit.Test;

import kiss.ThrowableFunction;

/**
 * @version 2017/02/02 12:09:38
 */
public class NameValueTest {

    @Test
    public void string() {
        string(key -> "value");
    }

    private void string(ThrowableFunction param) {
        assert param.parameterName(0).equals("key");
        assert param.apply(null).equals("value");
    }

    @Test
    public void integer() {
        integer(value -> 10);
    }

    private void integer(ThrowableFunction<Integer, Integer> param) {
        assert param.parameterName(0).equals("value");
        assert param.apply(null) == 10;
    }
}
