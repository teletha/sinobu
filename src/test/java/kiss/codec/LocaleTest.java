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

import java.util.Locale;

import org.junit.Test;

import kiss.Decoder;
import kiss.Encoder;
import kiss.model.Model;

/**
 * @version 2015/10/20 11:18:24
 */
public class LocaleTest {

    @Test
    public void codec() throws Exception {
        Decoder<Locale> decoder = Model.of(Locale.class).decoder();
        Encoder<Locale> encoder = Model.of(Locale.class).encoder();
        assert decoder.decode("en") == Locale.ENGLISH;
        assert encoder.encode(Locale.ENGLISH).equals("en");
    }
}
