/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.file;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;
import kiss.I;

/**
 * @version 2015/07/02 13:48:37
 */
public class LocateTest {

    @Rule
    @ClassRule
    public static final CleanRoom room = new CleanRoom("file");

    @Test
    public void locateRelative() {
        Path path = I.locate("context");
        assert!path.isAbsolute();
    }

    @Test
    public void locateAbsolute() {
        Path path = I.locate(room.root.toAbsolutePath().toString());
        assert path.isAbsolute();
    }

    @Test
    public void locateWhitespace() throws MalformedURLException {
        Path path = I.locate(new URL("file://white space"));
        assert!path.isAbsolute();
    }

    @Test
    public void locateTemporary() {
        Path path = I.locateTemporary();
        assert!Files.exists(path);
        assert!Files.isDirectory(path);
        assert!Files.isRegularFile(path);
    }

    @Test
    public void locateTemporaries() {
        Path path1 = I.locateTemporary();
        Path path2 = I.locateTemporary();
        Path path3 = I.locateTemporary();
        assert!Files.exists(path1);
        assert!Files.exists(path2);
        assert!Files.exists(path3);
        assert path1.getFileName() != path2.getFileName();
        assert path3.getFileName() != path2.getFileName();
        assert path3.getFileName() != path1.getFileName();
    }
}
