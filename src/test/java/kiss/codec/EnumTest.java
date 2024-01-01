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

import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

import kiss.Decoder;
import kiss.I;
import kiss.LoadableTestBase;

class EnumTest extends LoadableTestBase {

    @Test
    void codec() {
        Decoder decoder = I.find(Decoder.class, RetentionPolicy.class);
        assert decoder != null;
    }

    @Test
    void builtin() {
        assert I.transform(RetentionPolicy.CLASS, String.class) == "CLASS";
        assert I.transform("RUNTIME", RetentionPolicy.class) == RetentionPolicy.RUNTIME;
    }

    @Test
    void overrideToString() {
        assert I.transform("DontUseToStringMethod", EnumUseNameMethod.class) == EnumUseNameMethod.DontUseToStringMethod;
        assert I.transform(EnumUseNameMethod.DontUseToStringMethod, String.class) == "DontUseToStringMethod";
    }

    /**
     * 
     */
    private static enum EnumUseNameMethod {

        DontUseToStringMethod("#toString is overridden");

        private final String name;

        /**
         * @param name
         */
        private EnumUseNameMethod(String name) {
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

    @Test
    void extended() {
        // by enum-type
        assert I.transform("A", Extended.class).value().equals("A");

        // by sub-type
        assert I.transform("A", Extended.A.getClass()).value().equals("A");
        assert I.transform("B", Extended.B.getClass()).value().equals("B");
    }

    private enum Extended {
        A {
            @Override
            String value() {
                return "A";
            }
        },
        B {
            @Override
            String value() {
                return "B";
            }
        };

        abstract String value();
    }
}