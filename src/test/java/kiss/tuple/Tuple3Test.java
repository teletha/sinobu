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
import kiss.Ⅲ;

/**
 * @version 2016/03/27 15:18:54
 */
public class Tuple3Test {

    @Test
    public void equality() {
        Ⅲ<String, Integer, Long> value1 = I.pair("String", 10, 30L);
        Ⅲ<String, Integer, Long> value2 = I.pair("String", 10, 30L);

        assert value1 != value2;
        assert value1.equals(value2);
        assert value1.hashCode() == value2.hashCode();
    }
}
