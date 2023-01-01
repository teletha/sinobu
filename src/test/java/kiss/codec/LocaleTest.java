/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.codec;

import java.util.Locale;

import org.junit.jupiter.api.Test;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;

/**
 * @version 2015/10/20 11:18:24
 */
public class LocaleTest {

    @Test
    public void codec() throws Exception {
        Decoder<Locale> decoder = I.find(Decoder.class, Locale.class);
        Encoder<Locale> encoder = I.find(Encoder.class, Locale.class);
        assert decoder.decode("en") == Locale.ENGLISH;
        assert encoder.encode(Locale.ENGLISH).equals("en");
    }
}