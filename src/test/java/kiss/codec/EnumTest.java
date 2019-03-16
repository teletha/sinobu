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
import kiss.LoadableTestBase;

class EnumTest extends LoadableTestBase {

    @Test
    void codec() {
        Decoder<OverrideToString> decoder = I.find(Decoder.class, OverrideToString.class);
        Encoder<OverrideToString> encoder = I.find(Encoder.class, OverrideToString.class);
        assert decoder.decode("A") == OverrideToString.A;
        assert encoder.encode(OverrideToString.A).equals("A");
    }

    @Test
    void custom() {
        loadClasses();

        Decoder<Custom> decoder = I.find(Decoder.class, Custom.class);
        Encoder<Custom> encoder = I.find(Encoder.class, Custom.class);
        assert decoder.decode("OK") == Custom.OK;
        assert decoder.decode("Ok") == Custom.OK;
        assert decoder.decode("ok") == Custom.OK;
        assert encoder.encode(Custom.OK).equals("ok");
    }

    /**
     * 
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

    /**
     * 
     */
    private static enum Custom {
        OK, NG;
    }

    /**
     * 
     */
    @SuppressWarnings("unused")
    private static class CustomCodec implements Decoder<Custom>, Encoder<Custom> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(Custom value) {
            return value.toString().toLowerCase();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Custom decode(String value) {
            switch (value.toLowerCase()) {
            case "ok":
                return Custom.OK;

            case "ng":
                return Custom.NG;
            }
            // If this exception will be thrown, it is bug of this program. So we must rethrow the
            // wrapped error in here.
            throw new Error();
        }
    }
}
