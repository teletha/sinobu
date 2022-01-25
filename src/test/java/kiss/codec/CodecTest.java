/*
 * Copyright (C) 2022 The SINOBU Development Team
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

class CodecTest {

    @Test
    void Boolean() {
        assert codec(Boolean.TRUE);
    }

    @Test
    void Integer() {
        assert codec(Integer.valueOf(1234));
    }

    @Test
    void Long() {
        assert codec(Long.valueOf(1234));
    }

    @Test
    void Float() {
        assert codec(Float.valueOf(1.234f));
    }

    @Test
    void Double() {
        assert codec(Double.valueOf(1.234d));
    }

    @Test
    void Byte() {
        assert codec(Byte.valueOf((byte) 1));
    }

    @Test
    void Short() {
        assert codec(Short.valueOf((short) 1));
    }

    @Test
    void Character() {
        assert codec(Character.valueOf('c'));
    }

    private <T> boolean codec(T value) {
        Encoder<T> encoder = I.find(Encoder.class, value.getClass());
        Decoder<T> decoder = I.find(Decoder.class, value.getClass());
        assert decoder.decode(encoder.encode(value)).equals(value);

        return true;
    }
}