/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.codec;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import kiss.Decoder;
import kiss.Disposable;
import kiss.Encoder;
import kiss.LoadableTestBase;
import kiss.model.Model;

class UpdateTest {

    @Test
    void updateCodecDynamically() {
        // check built-in enum codec
        Model<Code> model = Model.of(Code.class);
        assert model.encode(Code.BuiltIn) == "BuiltIn";
        assert model.decode("BuiltIn") == Code.BuiltIn;

        // load codec dynamically
        Disposable unload = LoadableTestBase.load(DynamicCodec.class);
        assert model.encode(Code.BuiltIn) == "Updated";
        assert model.decode("Replaced codec accepts any word") == Code.BuiltIn;

        // unload codec dynamically
        unload.dispose();
        assert model.encode(Code.BuiltIn) == "BuiltIn";
        assert model.decode("BuiltIn") == Code.BuiltIn;
        assertThrows(IllegalArgumentException.class, () -> model.decode("Replaced codec accepts any word"));
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
