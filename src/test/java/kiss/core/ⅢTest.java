/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import org.junit.Test;

import kiss.Ⅱ;
import kiss.I;
import kiss.Ⅲ;

/**
 * @version 2015/04/10 1:36:39
 */
public class ⅢTest {

    @Test
    public void changeFirstValue() throws Exception {
        Ⅲ<String, Integer, Long> value = I.pair("String", 10, 30L);

        value = value.a("Replace");
        assert value.ⅰ.equals("Replace");
        assert value.ⅱ == 10;
        assert value.ⅲ == 30L;
    }

    @Test
    public void changeSecondValue() throws Exception {
        Ⅲ<String, Integer, Long> value = I.pair("String", 10, 30L);

        value = value.e(20);
        assert value.ⅰ.equals("String");
        assert value.ⅱ == 20;
        assert value.ⅲ == 30L;
    }

    @Test
    public void changeThirdValue() throws Exception {
        Ⅲ<String, Integer, Long> value = I.pair("String", 10, 30L);

        value = value.o(100L);
        assert value.ⅰ.equals("String");
        assert value.ⅱ == 10;
        assert value.ⅲ == 100L;
    }

    @Test
    public void removeFirstValue() throws Exception {
        Ⅲ<String, Integer, Long> value = I.pair("String", 10, 30L);
        Ⅱ<Integer, Long> removed = value.á();
        assert removed.ⅰ == 10;
        assert removed.ⅱ == 30L;
    }

    @Test
    public void removeSecondValue() throws Exception {
        Ⅲ<String, Integer, Long> value = I.pair("String", 10, 30L);
        Ⅱ<String, Long> removed = value.é();
        assert removed.ⅰ.equals("String");
        assert removed.ⅱ == 30L;
    }

    @Test
    public void removeThirdValue() throws Exception {
        Ⅲ<String, Integer, Long> value = I.pair("String", 10, 30L);
        Ⅱ<String, Integer> removed = value.ó();
        assert removed.ⅰ.equals("String");
        assert removed.ⅱ == 10;
    }

    @Test
    public void calculateFirstValue() throws Exception {
        Ⅲ<String, Integer, Long> value = I.pair("String", 10, 30L);
        value = value.a(v -> v + " Type");

        assert value.ⅰ.equals("String Type");
        assert value.ⅱ == 10;
        assert value.ⅲ == 30L;
    }

    @Test
    public void calculateSecondValue() throws Exception {
        Ⅲ<String, Integer, Long> value = I.pair("String", 10, 30L);
        value = value.e(v -> v + 10);

        assert value.ⅰ.equals("String");
        assert value.ⅱ == 20;
        assert value.ⅲ == 30L;
    }

    @Test
    public void calculateThirdValue() throws Exception {
        Ⅲ<String, Integer, Long> value = I.pair("String", 10, 30L);
        value = value.o(v -> v * 30);

        assert value.ⅰ.equals("String");
        assert value.ⅱ == 10;
        assert value.ⅲ == 900L;
    }

    @Test
    public void equality() {
        Ⅲ<String, Integer, Long> value1 = I.pair("String", 10, 30L);
        Ⅲ<String, Integer, Long> value2 = I.pair("String", 10, 30L);

        assert value1 != value2;
        assert value1.equals(value2);
        assert value1.hashCode() == value2.hashCode();
    }
}
