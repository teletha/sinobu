/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2012/10/15 14:30:16
 */
public class PreferenceTest {

    public static final Path root = I.locateTemporary();

    @Rule
    public static final SinobuSetting env = new SinobuSetting(root);

    @Test
    public void notExist() throws Exception {
        Path path = I.locate(NotExist.class);
        assert Files.notExists(path);

        NotExist instance = I.make(NotExist.class);
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
