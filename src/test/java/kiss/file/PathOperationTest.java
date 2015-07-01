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

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;
import kiss.I;

/**
 * @version 2015/07/01 12:45:10
 */
public class PathOperationTest {

    @Rule
    @ClassRule
    public static final CleanRoom room = new CleanRoom("file");

    @Test
    public void locateRelative() throws Exception {
        Path path = I.locate("context");
        assert!path.isAbsolute();
    }

    @Test
    public void locateAbsolute() throws Exception {
        Path path = I.locate(room.root.toAbsolutePath().toString());
        assert path.isAbsolute();
    }

    @Test
    public void locateWhitespace() throws Exception {
        Path path = I.locate(new URL("file://white space"));
        assert!path.isAbsolute();
    }

    @Test
    public void deleteFile() {
        Path input = room.locateFile("test01/01.txt");

        assert Files.exists(input);

        // operation
        I.delete(input);

        assert Files.notExists(input);
    }

    @Test
    public void deleteDirectory() {
        Path input = room.locateDirectory("test01");

        assert Files.exists(input);
        assert Files.exists(input.resolve("01.txt"));
        assert Files.exists(input.resolve("directory1/01.txt"));

        // operation
        I.delete(input);

        assert Files.exists(input.getParent());
        assert Files.notExists(input);
        assert Files.notExists(input.resolve("01.txt"));
        assert Files.notExists(input.resolve("directory1/01.txt"));
    }

    @Test
    public void deleteDirectoryChildren() {
        Path input = room.locateDirectory("test01");

        assert Files.exists(input);
        assert Files.exists(input.resolve("01.txt"));
        assert Files.exists(input.resolve("directory1/01.txt"));

        // operation
        I.delete(input, "**");

        assert Files.exists(input.getParent());
        assert Files.exists(input);
        assert Files.notExists(input.resolve("01.txt"));
        assert Files.notExists(input.resolve("directory1/01.txt"));
    }

    @Test
    public void deleteAbsent() {
        Path input = room.locateAbsent("absent");

        assert Files.notExists(input);

        // operation
        I.delete(input);

        assert Files.notExists(input);
    }

    @Test
    public void deleteNull() throws Exception {
        I.delete(null);
    }

    @Test
    public void createTemporary() throws Exception {
        Path path = I.locateTemporary();
        assert!Files.exists(path);
        assert!Files.isDirectory(path);
        assert!Files.isRegularFile(path);
    }

    @Test
    public void createTemporaries() throws Exception {
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
