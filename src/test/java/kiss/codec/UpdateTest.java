/*
 * Copyright (C) 2020 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.codec;

import static org.junit.jupiter.api.Assertions.*;

import kiss.Decoder;
import kiss.Disposable;
import kiss.Encoder;
import kiss.I;
import kiss.LoadableTestBase;

class UpdateTest {

    void updateCodecDynamically() {
        // check built-in enum codec
        assert I.transform(Code.BuiltIn, String.class) == "BuiltIn";
        assert I.transform("BuiltIn", Code.class) == Code.BuiltIn;

        // load codec dynamically
        Disposable unload = LoadableTestBase.load(DynamicCodec.class);
        assert I.transform(Code.BuiltIn, String.class) == "Updated";
        assert I.transform("Replaced codec accepts any word", Code.class) == Code.BuiltIn;

        // unload codec dynamically
        unload.dispose();
        assert I.transform(Code.BuiltIn, String.class) == "BuiltIn";
        assert I.transform("BuiltIn", Code.class) == Code.BuiltIn;
        assertThrows(IllegalArgumentException.class, () -> I.transform("Replaced codec accepts any word", Code.class));
    }

    private static enum Code {
        BuiltIn;
    }

    private static class DynamicCodec implements Decoder<Code>, Encoder<Code> {

        @Override
        public String encode(Code value) {
            return "Updated";
        }

        @Override
        public Code decode(String value) {
            return Code.BuiltIn;
        }
    }
}