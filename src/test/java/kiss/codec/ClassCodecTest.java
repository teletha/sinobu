/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.codec;

import org.junit.jupiter.api.Test;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;

class ClassCodecTest {

    private final Decoder<Class<?>> decoder = I.find(Decoder.class, Class.class);

    private final Encoder<Class<?>> encoder = I.find(Encoder.class, Class.class);

    @Test
    void systemClass() {
        assertClass(String.class);
    }

    @Test
    void systemClassArray() {
        assertClass(String[].class);
    }

    @Test
    void userClass() {
        assertClass(ClassCodecTest.class);
    }

    @Test
    void userClassArray() {
        assertClass(ClassCodecTest[].class);
    }

    @Test
    void primitiveClass() {
        assertClass(int.class);
    }

    @Test
    void primitiveClassArray() {
        assertClass(int[].class);
    }

    /**
     * Helper method to test.
     */
    private void assertClass(Class<?> clazz) {
        String encoded = encoder.encode(clazz);
        assert encoded != null;
        assert decoder.decode(encoded) == clazz;
    }
}