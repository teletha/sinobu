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

import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;
import kiss.I;

/**
 * @version 2015/07/04 21:33:37
 */
public class GlobPatternTest {

    @Rule
    public CleanRoom room = new CleanRoom();

    @Test
    public void topLevelWildcard() {
        Path path = room.locateDirectory("root", $ -> {
            $.file("file1");
            $.file("file2");
            $.dir("dir", () -> {
                $.file("file3");
                $.file("text");
            });
        });

        // assert I.walk(path, "*").size() == 2;
        // assert I.walk(path, "*/text").size() == 1;
        // assert I.walk(path, "*", "*/text").size() == 3;
        assert I.walk(path, (p, attr) -> attr.isDirectory()).size() == 1;
    }
}
