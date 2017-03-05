/*
 * Copyright (C) 2017 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.jdk;

import static java.nio.file.StandardCopyOption.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;

/**
 * @version 2011/03/22 16:55:29
 */
public class FilesTest {

    @Rule
    @ClassRule
    public static final CleanRoom room = new CleanRoom();

    @Test(expected = NullPointerException.class)
    public void existNull() throws Exception {
        Files.exists(null); // lololol
    }

    @Test
    public void copyEmptyDirectoryIntoAbsentDirectory() throws Exception {
        Path input = room.locateDirectory("test");
        Path output = room.locateAbsent("out");
        assert Files.isDirectory(input);

        Files.copy(input, output);

        assert Files.isDirectory(output);
    }

    @Test
    public void copyEmptyDirectoryIntoPresentDirectory() throws Exception {
        Path input = room.locateDirectory("test");
        Path output = room.locateDirectory("out");
        assert Files.isDirectory(input);

        Files.copy(input, output, REPLACE_EXISTING);

        assert Files.isDirectory(output);
    }

    @Test
    public void copyDirectory() throws Exception {
        Path input = room.locateDirectory("test/child");
        Path output = room.locateAbsent("out");

        assert Files.isDirectory(input);
        assert Files.isDirectory(input.getParent());

        Files.copy(input, output, REPLACE_EXISTING);

        assert Files.isDirectory(output);
        assert!Files.isDirectory(output.resolve("test"));
        assert!Files.isDirectory(output.resolve("child"));
    }
}
