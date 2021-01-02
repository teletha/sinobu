/*
 * Copyright (C) 2021 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import kiss.I;

/**
 * @version 2017/03/30 22:11:54
 */
public class CopyTest {

    @Test
    public void stream() throws Exception {
        byte[] bytes = new byte[] {0, 1, 2, 3};
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        assert out.toByteArray().length == 0;
        I.copy(in, out, true);
        assert Arrays.equals(bytes, out.toByteArray());
    }

    @Test
    public void readable() throws Exception {
        String value = "test";
        StringReader in = new StringReader(value);
        StringWriter out = new StringWriter();

        assert out.toString().length() == 0;
        I.copy(in, out, true);
        assert out.toString().equals(value);
    }
}