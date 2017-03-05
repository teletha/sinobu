/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.lifestyle;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import kiss.I;
import kiss.Manageable;
import kiss.Preference;

/**
 * @version 2017/03/05 22:27:31
 */
public class PreferenceTest {

    private static Path original;

    @BeforeClass
    public static void init() {
        original = I.$working;
        I.$working = I.locateTemporary();
    }

    @AfterClass
    public static void clean() {
        I.$working = original;
    }

    @Test
    public void notExist() throws Exception {
        Path path = I.$working.resolve("preferences").resolve(NotExist.class.getName().concat(".xml"));
        assert Files.notExists(path);

        NotExist instance = I.make(NotExist.class);
        assert instance != null;
    }

    @Test
    public void sizeZero() throws Exception {
        Path path = I.$working.resolve("preferences").resolve(SizeZero.class.getName().concat(".xml"));
        Files.createDirectories(path.getParent());
        Files.createFile(path);
        assert Files.exists(path);
        assert Files.size(path) == 0;

        SizeZero instance = I.make(SizeZero.class);
        assert instance != null;
    }

    /**
     * @version 2012/10/15 14:30:35
     */
    @Manageable(lifestyle = Preference.class)
    private static class NotExist {
    }

    /**
     * @version 2012/10/15 14:30:35
     */
    @Manageable(lifestyle = Preference.class)
    private static class SizeZero {
    }
}
