/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.codec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

import kiss.I;
import kiss.model.Model;

/**
 * @version 2017/03/29 17:28:43
 */
public class PathCodecTest {

    @Test
    public void relative() throws Exception {
        assertPath(Paths.get("test"));
    }

    @Test
    public void absolute() throws Exception {
        assertPath(Paths.get("test").toAbsolutePath());
    }

    /**
     * <p>
     * Helper method to test.
     * </p>
     * 
     * @param clazz
     */
    private void assertPath(Path path) {
        try {
            String encoded = Model.of(path).encode(path);
            assert encoded != null;
            assert Files.isSameFile(path, Model.of(path).decode(encoded));
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }
}
