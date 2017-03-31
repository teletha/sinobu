/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.extension;

import java.util.List;

import org.junit.Test;

import kiss.Extensible;
import kiss.I;

/**
 * @version 2017/03/29 9:43:20
 */
public class LoadTest {

    @Test
    public void twice() throws Exception {
        I.load(LoadTest.class, true);

        List<Ex> find = I.find(Ex.class);
        assert find.size() == 1;

        I.load(LoadTest.class, true);

        find = I.find(Ex.class);
        assert find.size() == 1;
    }

    /**
     * @version 2017/03/29 9:43:44
     */
    private static class Ex implements Extensible {
    }
}
