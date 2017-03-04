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
import kiss.Disposable;
import kiss.Encoder;
import kiss.I;
import kiss.model.Model;

/**
 * @version 2016/06/01 17:17:38
 */
public class CodecUpdateTest {

    @Test
    public void loadAndUnload() throws Exception {
        Disposable unloader = I.load(CodecUpdateTest.class, true);

        Model<Integer> model = Model.of(Integer.class);
        assert model.encode(10).equals("UserDefinedCodec");
        assert model.decode("10") == 0;

        unloader.dispose();

        assert model.encode(10).equals("10");
        assert model.decode("10") == 10;
    }

    /**
     * @version 2016/06/01 17:12:10
     */
    @SuppressWarnings("unused")
    private static class UserDefinedCodec implements Encoder<Integer>, Decoder<Integer> {

        /**
         * {@inheritDoc}
         */
        @Override
        public String encode(Integer value) {
            return "UserDefinedCodec";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Integer decode(String value) {
            return 0;
        }
    }
}
