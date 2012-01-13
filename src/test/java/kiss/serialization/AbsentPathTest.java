/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.serialization;

import hub.CleanRoom;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;

import kiss.I;
import kiss.sample.bean.Person;

/**
 * @version 2011/06/13 12:27:57
 */
public class AbsentPathTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    private Person person = new Person();

    @Test
    public void write() throws Exception {
        Path path = room.locateAbsent("file");
        I.write(person, path, false);

        assert Files.exists(path);
    }

    @Test
    public void writeNest() throws Exception {
        Path path = room.locateAbsent("dir/file");
        I.write(person, path, false);

        assert Files.exists(path);
    }

    @Test(expected = NoSuchFileException.class)
    public void read() throws Exception {
        Path path = room.locateAbsent("file");
        I.read(path, person);
    }

    @Test(expected = NoSuchFileException.class)
    public void readNest() throws Exception {
        Path path = room.locateAbsent("dir/file");
        I.read(path, person);
    }
}
