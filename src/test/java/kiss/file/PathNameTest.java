/*
 * Copyright (C) 2013 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.file;

import kiss.I;

import org.junit.Test;

/**
 * @version 2013/03/25 11:33:55
 */
public class PathNameTest {

    @Test
    public void normal() throws Exception {
        String[] names = I.call(I.locate("test.txt"));
        assert names[0].equals("test");
        assert names[1].equals("txt");
    }

    @Test
    public void noExtension() throws Exception {
        String[] names = I.call(I.locate("test"));
        assert names[0].equals("test");
        assert names[1].equals("");
    }

    @Test
    public void noFile() throws Exception {
        String[] names = I.call(I.locate(".hgignore"));
        assert names[0].equals("");
        assert names[1].equals("hgignore");
    }

    @Test
    public void empty() throws Exception {
        String[] names = I.call(I.locate(""));
        assert names[0].equals("");
        assert names[1].equals("");
    }

    @Test
    public void dot() throws Exception {
        String[] names = I.call(I.locate("."));
        assert names[0].equals("");
        assert names[1].equals("");
    }

    @Test
    public void absolute() throws Exception {
        String[] names = I.call(I.locate("test.txt").toAbsolutePath());
        assert names[0].equals("test");
        assert names[1].equals("txt");
    }

    @Test(expected = NullPointerException.class)
    public void illegal() throws Exception {
        I.call(null);
    }
}
