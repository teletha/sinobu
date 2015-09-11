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

import org.junit.Test;

import kiss.Binary;
import kiss.I;
import kiss.Ternary;

/**
 * @version 2015/04/10 1:36:39
 */
public class TernaryTest {

    @Test
    public void changeFirstValue() throws Exception {
        Ternary<String, Integer, Long> value = I.pair("String", 10, 30L);

        value = value.a("Replace");
        assert value.a.equals("Replace");
        assert value.e == 10;
        assert value.o == 30L;
    }

    @Test
    public void changeSecondValue() throws Exception {
        Ternary<String, Integer, Long> value = I.pair("String", 10, 30L);

        value = value.e(20);
        assert value.a.equals("String");
        assert value.e == 20;
        assert value.o == 30L;
    }

    @Test
    public void changeThirdValue() throws Exception {
        Ternary<String, Integer, Long> value = I.pair("String", 10, 30L);

        value = value.o(100L);
        assert value.a.equals("String");
        assert value.e == 10;
        assert value.o == 100L;
    }

    @Test
    public void removeFirstValue() throws Exception {
        Ternary<String, Integer, Long> value = I.pair("String", 10, 30L);
        Binary<Integer, Long> removed = value.á();
        assert removed.a == 10;
        assert removed.e == 30L;
    }

    @Test
    public void removeSecondValue() throws Exception {
        Ternary<String, Integer, Long> value = I.pair("String", 10, 30L);
        Binary<String, Long> removed = value.é();
        assert removed.a.equals("String");
        assert removed.e == 30L;
    }

    @Test
    public void removeThirdValue() throws Exception {
        Ternary<String, Integer, Long> value = I.pair("String", 10, 30L);
        Binary<String, Integer> removed = value.ó();
        assert removed.a.equals("String");
        assert removed.e == 10;
    }

    @Test
    public void calculateFirstValue() throws Exception {
        Ternary<String, Integer, Long> value = I.pair("String", 10, 30L);
        value = value.a(v -> v + " Type");

        assert value.a.equals("String Type");
        assert value.e == 10;
        assert value.o == 30L;
    }

    @Test
    public void calculateSecondValue() throws Exception {
        Ternary<String, Integer, Long> value = I.pair("String", 10, 30L);
        value = value.e(v -> v + 10);

        assert value.a.equals("String");
        assert value.e == 20;
        assert value.o == 30L;
    }

    @Test
    public void calculateThirdValue() throws Exception {
        Ternary<String, Integer, Long> value = I.pair("String", 10, 30L);
        value = value.o(v -> v * 30);

        assert value.a.equals("String");
        assert value.e == 10;
        assert value.o == 900L;
    }

    @Test
    public void equality() {
        Ternary<String, Integer, Long> value1 = I.pair("String", 10, 30L);
        Ternary<String, Integer, Long> value2 = I.pair("String", 10, 30L);

        assert value1 != value2;
        assert value1.equals(value2);
        assert value1.hashCode() == value2.hashCode();
    }
}
