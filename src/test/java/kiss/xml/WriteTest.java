/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.xml;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Test;

import kiss.I;

/**
 * @version 2017/04/02 13:40:52
 */
public class WriteTest {

    @Test
    public void xml() throws Exception {
        Path file = I.locateTemporary();
        I.xml("root").to(Files.newBufferedWriter(file));

        assert Files.exists(file);
        assert Files.size(file) != 0;
    }
}