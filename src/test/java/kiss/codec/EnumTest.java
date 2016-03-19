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
import kiss.model.Model;

/**
 * @version 2015/10/20 17:47:06
 */
public class EnumTest {

    @Test
    public void codec() throws Exception {
        Decoder<OverrideToString> decoder = Model.of(OverrideToString.class).getDecoder();
        Encoder<OverrideToString> encoder = Model.of(OverrideToString.class).getEncoder();
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
