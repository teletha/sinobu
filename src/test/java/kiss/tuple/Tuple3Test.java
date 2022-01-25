/*
 * Copyright (C) 2022 The SINOBU Development Team
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
    void map() {
        Ⅲ<String, Integer, Long> value = I.pair("test", 0, 1L);
        String concat = value.map((one, two, three) -> {
            return one + two + three;
        });
        assert concat.equals("test01");
    }

    @Test
    void equality() {
        Ⅲ<String, Integer, Long> value = I.pair("String", 10, 30L);
        Ⅲ<String, Integer, Long> other = I.pair("String", 10, 30L);
        Ⅲ<Long, Integer, String> inverse = I.pair(30L, 10, "String");
        assert value.equals(other);
        assert value.equals(inverse) == false;
    }

    @Test
    void hash() {
        Ⅲ<String, Integer, Long> value = I.pair("String", 10, 30L);
        Ⅲ<String, Integer, Long> other = I.pair("String", 10, 30L);
        Ⅲ<Long, Integer, String> inverse = I.pair(30L, 10, "String");
        assert value.hashCode() == other.hashCode();
        assert value.hashCode() != inverse.hashCode();
    }
}