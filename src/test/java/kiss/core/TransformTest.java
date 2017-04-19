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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Date;
import java.util.Locale;

import org.junit.Test;

import kiss.I;

/**
 * @version 2016/05/04 1:17:02
 */
public class TransformTest {

    @Test
    public void inputNull() throws Exception {
        assert I.transform(null, int.class) == null;
        assert I.transform(null, String.class) == null;
    }

    @Test(expected = NullPointerException.class)
    public void outputNull() throws Exception {
        assert I.transform("1", null) == null;
    }

    @Test
    public void primitiveInt() {
        assert I.transform("1", int.class) == 1;
        assert I.transform(1, String.class).equals("1");
    }

    @Test
    public void primitiveLong() {
        assert I.transform("1", long.class) == 1L;
        assert I.transform(1L, String.class).equals("1");
    }

    @Test
    public void primitiveChar() {
        assert I.transform("1", char.class) == '1';
        assert I.transform('1', String.class).equals("1");
    }

    @Test
    public void primitiveFloat() {
        assert I.transform("1.3", float.class) == 1.3f;
        assert I.transform(1.3f, String.class).equals("1.3");
    }

    @Test
    public void primitiveDouble() {
        assert I.transform("1.3", double.class) == 1.3d;
        assert I.transform(1.3d, String.class).equals("1.3");
    }

    @Test
    public void primitiveBoolean() {
        assert I.transform("true", boolean.class);
        assert I.transform(true, String.class).equals("true");
    }

    @Test
    public void date() throws Exception {
        assert I.transform(new Date(0), String.class).equals("1970-01-01T09:00:00");
        assert I.transform("1970-01-01T09:00:00", Date.class).equals(new Date(0));
    }

    @Test
    public void url() throws Exception {
        URL value = new URL("http://localhost:8888/");
        String text = "http://localhost:8888/";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, URL.class).equals(value);
    }

    @Test
    public void uri() throws Exception {
        URI value = new URI("http://localhost:8888/");
        String text = "http://localhost:8888/";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, URI.class).equals(value);
    }

    @Test
    public void locale() throws Exception {
        Locale value = new Locale("en");
        String text = "en";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, Locale.class).equals(value);
    }

    @Test
    public void bigInteger() throws Exception {
        BigInteger value = new BigInteger("12345678901234567890");
        String text = "12345678901234567890";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, BigInteger.class).equals(value);
    }

    @Test
    public void bigDecimal() throws Exception {
        BigDecimal value = new BigDecimal("123.456789012345678901");
        String text = "123.456789012345678901";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, BigDecimal.class).equals(value);
    }

    @Test
    public void stringBuilder() throws Exception {
        StringBuilder value = new StringBuilder("123.456789012345678901");
        String text = "123.456789012345678901";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, StringBuilder.class).toString().equals(value.toString());
    }

    @Test
    public void stringBuffer() throws Exception {
        StringBuffer value = new StringBuffer("123.456789012345678901");
        String text = "123.456789012345678901";

        assert I.transform(value, String.class).equals(text);
        assert I.transform(text, StringBuffer.class).toString().equals(value.toString());
    }
}
