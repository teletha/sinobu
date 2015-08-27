/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import org.junit.Test;

/**
 * @version 2015/08/27 11:03:09
 */
public class NameValueTest {

    @Test
    public void string() {
        string(key -> "value");
    }

    private void string(NamedValue param) {
        assert param.name().equals("key");
        assert param.value().equals("value");
    }

    @Test
    public void integer() {
        integer(key -> 10);
    }

    private void integer(NamedValue<Integer> param) {
        assert param.name().equals("key");
        assert param.value() == 10;
    }
}
