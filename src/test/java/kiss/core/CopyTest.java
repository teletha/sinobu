/*
 * Copyright (C) 2023 The SINOBU Development Team
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import kiss.I;

class CopyTest {

    /**
     * @see I#copy(InputStream, OutputStream, boolean)
     */
    @Test
    void copyInputStream() {
        byte[] bytes = {0, 1, 2, 3};
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        I.copy(in, out, true);

        assert Arrays.equals(bytes, out.toByteArray());
    }

    /**
     * @see I#copy(Readable, Appendable, boolean)
     */
    @Test
    void copyReadable() {
        String value = "test";
        StringReader in = new StringReader(value);
        StringWriter out = new StringWriter();

        I.copy(in, out, true);

        assert out.toString().equals(value);
    }
}