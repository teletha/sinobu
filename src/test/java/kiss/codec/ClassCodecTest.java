/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.codec;

import org.junit.Test;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;

/**
 * @version 2017/03/04 16:23:33
 */
public class ClassCodecTest {

    private Decoder<Class> decoder = I.find(Decoder.class, Class.class);

    private Encoder<Class> encoder = I.find(Encoder.class, Class.class);

    @Test
    public void systemClass() throws Exception {
        assertClass(String.class);
    }

    @Test
    public void systemClassArray() throws Exception {
        assertClass(String[].class);
    }

    @Test
    public void userClass() throws Exception {
        assertClass(ClassCodecTest.class);
    }

    @Test
    public void userClassArray() throws Exception {
        assertClass(ClassCodecTest[].class);
    }

    @Test
    public void primitiveClass() throws Exception {
        assertClass(int.class);
    }

    @Test
    public void primitiveClassArray() throws Exception {
        assertClass(int[].class);
    }

    /**
     * <p>
     * Helper method to test.
     * </p>
     * 
     * @param clazz
     */
    private void assertClass(Class clazz) {
        String encoded = encoder.encode(clazz);
        assert encoded != null;
        assert decoder.decode(encoded) == clazz;
    }
}
