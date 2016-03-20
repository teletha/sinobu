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

import java.nio.file.attribute.FileTime;

import org.junit.Test;

import kiss.Decoder;
import kiss.Encoder;
import kiss.model.Model;

/**
 * @version 2016/01/20 11:04:29
 */
public class FileTimeTest {

    @Test
    public void codec() throws Exception {
        Decoder<FileTime> decoder = Model.of(FileTime.class).decoder();
        Encoder<FileTime> encoder = Model.of(FileTime.class).encoder();
        assert decoder.decode("0").equals(FileTime.fromMillis(0));
        assert encoder.encode(FileTime.fromMillis(0)).equals("0");
    }
}
