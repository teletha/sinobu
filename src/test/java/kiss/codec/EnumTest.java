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

import java.lang.annotation.RetentionPolicy;

import org.junit.jupiter.api.Test;

import kiss.LoadableTestBase;
import kiss.model.Model;

class EnumTest extends LoadableTestBase {

    @Test
    void builtin() {
        Model<RetentionPolicy> model = Model.of(RetentionPolicy.class);
        assert model.encode(RetentionPolicy.CLASS) == "CLASS";
        assert model.decode("RUNTIME") == RetentionPolicy.RUNTIME;
    }

    @Test
    void overrideToString() {
        Model<EnumUseNameMethod> model = Model.of(EnumUseNameMethod.class);
        assert model.decode("DontUseToStringMethod") == EnumUseNameMethod.DontUseToStringMethod;
        assert model.encode(EnumUseNameMethod.DontUseToStringMethod) == "DontUseToStringMethod";
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
}
