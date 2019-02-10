/*
 * Copyright (C) 2019 Nameless Production Committee
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

/**
 * @version 2017/04/01 0:16:34
 */
public class CodecTest {

    @Test
    public void Boolean() throws Exception {
        assert codec(Boolean.TRUE);
    }

    private <T> boolean codec(T value) {
        Encoder<T> encoder = I.find(Encoder.class, value.getClass());
        Decoder<T> decoder = I.find(Decoder.class, value.getClass());
        assert decoder.decode(encoder.encode(value)).equals(value);

        return true;
    }
}
