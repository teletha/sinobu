/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.codec;

import org.junit.Test;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;

/**
 * @version 2015/10/20 17:47:06
 */
public class EnumTest {

    @Test
    public void codec() throws Exception {
        Decoder<OverrideToString> decoder = I.find(Decoder.class, OverrideToString.class);
        Encoder<OverrideToString> encoder = I.find(Encoder.class, OverrideToString.class);
        assert decoder.decode("A") == OverrideToString.A;
        assert encoder.encode(OverrideToString.A).equals("A");
    }

    /**
     * @version 2015/10/20 17:47:47
     */
    private static enum OverrideToString {

        A("Modify toString");

        private final String name;

        /**
         * @param name
         */
        private OverrideToString(String name) {
            this.name = name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return name;
        }
    }
}
