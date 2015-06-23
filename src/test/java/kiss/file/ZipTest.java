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

import java.nio.file.Path;
import java.util.function.Consumer;

import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;
import antibug.CleanRoom.FileSystemDSL;
import kiss.I;

/**
 * @version 2015/06/23 23:14:32
 */
public class ZipTest {

    @Rule
    public CleanRoom room = new CleanRoom();

    private Consumer<FileSystemDSL> structure = $ -> {
        $.file("a.txt");
        $.file("b.txt");
        $.file("1.file");
    };

    @Test
    public void zip() throws Exception {
        room.with(structure);

        Path output = I.locate("a.zip").toAbsolutePath();

        I.zip(room.root, output, "**.file");

        System.out.println(output);
    }
}
