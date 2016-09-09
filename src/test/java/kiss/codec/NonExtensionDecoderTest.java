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

import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

import kiss.Decoder;
import kiss.I;

/**
 * @version 2016/07/31 9:12:23
 */
public class NonExtensionDecoderTest {

    @Test
    public void builtin() throws Exception {
        Decoder decoder = I.find(Decoder.class, RetentionPolicy.class);
        assert decoder != null;
    }
}
