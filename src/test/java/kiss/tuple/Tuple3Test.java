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
import kiss.Ⅲ;

class Tuple3Test {

    @Test
    void equality() {
        Ⅲ<String, Integer, Long> value1 = I.pair("String", 10, 30L);
        Ⅲ<String, Integer, Long> value2 = I.pair("String", 10, 30L);

        assert value1 != value2;
        assert value1.equals(value2);
        assert value1.hashCode() == value2.hashCode();
    }

    @Test
    void map() {
        Ⅲ<String, Integer, Long> value = I.pair("test", 0, 1L);
        String concat = value.map((one, two, three) -> {
            return one + two + three;
        });
        assert concat.equals("test01");
    }
}
