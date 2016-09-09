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

import org.junit.Ignore;
import org.junit.Test;

import kiss.Decoder;
import kiss.Encoder;
import kiss.I;

/**
 * @version 2016/01/20 11:04:29
 */
public class FileTimeTest {

    @Test
    @Ignore
    public void codec() throws Exception {
        Decoder<FileTime> decoder = I.find(Decoder.class, FileTime.class);
        Encoder<FileTime> encoder = I.find(Encoder.class, FileTime.class);
        assert decoder.decode("0").equals(FileTime.fromMillis(0));
        assert encoder.encode(FileTime.fromMillis(0)).equals("0");
    }
}
