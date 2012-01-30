/*
 * Copyright (C) 2012 Nameless Production Committee
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
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;
import antibug.Synchrotron;


import kiss.I;

/**
 * @version 2011/03/22 16:40:43
 */
public class PathOperationTest {

    @Rule
    public static final CleanRoom room = new CleanRoom("file");

    @Test
    public void locateRelative() throws Exception {
        Path path = I.locate("context");
        assert !path.isAbsolute();
    }

    @Test
    public void locateAbsolute() throws Exception {
        Path path = I.locate(room.root.toAbsolutePath().toString());
        assert path.isAbsolute();
    }

    @Test
    public void locateWhitespace() throws Exception {
        Path path = I.locate(new URL("file://white space"));
        assert !path.isAbsolute();
    }

    @Test
    public void copyFileToFile() throws Exception {
        Path input = room.locateFile("test01/01.txt");
        Path output = room.locateFile("out");
        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.areNotSameFile();

        // operation
        I.copy(input, output);

        // assert contents
        synchrotron.areSameFile();
    }

    @Test
    public void copyFileToDirectory() throws Exception {
        Path input = room.locateFile("test01/01.txt");
        Path output = room.locateDirectory("out");
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areNotSameFile();

        // operation
        I.copy(input, output);

        // assert contents
        synchrotron.areSameFile();
    }

    @Test
    public void copyFileToAbsent() throws Exception {
        Path input = room.locateFile("test01/01.txt");
        Path output = room.locateAbsent("out");
        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.areNotSameFile();

        // operation
        I.copy(input, output);

        // assert contents
        synchrotron.areSameFile();
    }

    @Test
    public void copyFileToAbsentDeeply() throws Exception {
        Path input = room.locateFile("test01/01.txt");
        Path output = room.locateAbsent("out/put/deeply");
        assert Files.notExists(output.getParent());
        assert Files.notExists(output.getParent().getParent());

        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.areNotSameFile();

        // operation
        I.copy(input, output);

        // assert contents
        synchrotron.areSameFile();
    }

    @Test(expected = NoSuchFileException.class)
    public void copyDirectoryToFile() throws Exception {
        Path input = room.locateDirectory("test01");
        Path output = room.locateFile("file");

        // operation
        I.copy(input, output);
    }

    @Test
    public void copyDirectoryToDirectory() throws Exception {
        Path input = room.locateDirectory("test01");
        Path output = room.locateDirectory("out");
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areNotSameDirectory();

        // operation
        I.copy(input, output);

        // assert contents
        synchrotron.areSameDirectory();
        synchrotron.child("01.txt").areSameFile();
        synchrotron.sibling("directory1").areSameDirectory();
        synchrotron.child("02.txt").areSameFile();
    }

    @Test
    public void copyDirectoryChildrenToDirectory() throws Exception {
        Path input = room.locateDirectory("test01");
        Path output = room.locateDirectory("out");
        Synchrotron synchrotron = new Synchrotron(input, output);

        // operation
        I.copy(input, output, "**");

        // assert contents
        synchrotron.child("01.txt").areSameFile();
        synchrotron.sibling("directory1").areSameDirectory();
        synchrotron.child("02.txt").areSameFile();
    }

    @Test
    public void copyDirectoryToDirectoryWithReplace() throws Exception {
        Path input = room.locateDirectory("test01");
        Path output = room.locateDirectory("out");
        Files.createFile(output.resolve("01.txt")); // create replaced file
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areNotSameDirectory();

        // operation
        I.copy(input, output);

        // assert contents
        synchrotron.areSameDirectory();
        synchrotron.child("01.txt").areSameFile();
        synchrotron.sibling("directory1").areSameDirectory();
        synchrotron.child("02.txt").areSameFile();
    }

    @Test
    public void copyDirectoryToAbsent() throws Exception {
        Path input = room.locateDirectory("test01");
        Path output = room.locateAbsent("out");

        // operation
        I.copy(input, output);

        // assert contents
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areSameDirectory();
        synchrotron.child("01.txt").areSameFile();
        synchrotron.sibling("directory1").areSameDirectory();
        synchrotron.child("02.txt").areSameFile();
    }

    @Test
    public void copyDirectoryToAbsentDeeply() throws Exception {
        Path input = room.locateDirectory("test01");
        Path output = room.locateAbsent("out/put/deeply");
        assert Files.notExists(output.getParent());
        assert Files.notExists(output.getParent().getParent());

        // operation
        I.copy(input, output);

        // assert contents
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areSameDirectory();
        synchrotron.child("01.txt").areSameFile();
        synchrotron.sibling("directory1").areSameDirectory();
        synchrotron.child("02.txt").areSameFile();
    }

    @Test(expected = NullPointerException.class)
    public void copyNullInput() throws Exception {
        Path input = null;
        Path output = room.locateAbsent("null");

        // operation
        I.copy(input, output);
    }

    @Test(expected = NullPointerException.class)
    public void copyNullOutput() throws Exception {
        Path input = room.locateAbsent("null");
        Path output = null;

        // operation
        I.copy(input, output);
    }

    @Test
    public void copyAbsentToFile() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateFile("out");

        // operation
        I.copy(input, output);
    }

    @Test
    public void copyAbsentToDirectory() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateDirectory("out");

        // operation
        I.copy(input, output);
    }

    @Test
    public void copyAbsentToAbsent() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateAbsent("out");

        // operation
        I.copy(input, output);
    }

    @Test
    public void moveFileToFile() throws Exception {
        Path input = room.locateFile("test01/01.txt");
        Path output = room.locateFile("out");
        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.areNotSameFile();

        // operation
        I.move(input, output);

        // assert contents
        synchrotron.exists(false, true);
    }

    @Test
    public void moveFileToDirectory() throws Exception {
        Path input = room.locateFile("test01/01.txt");
        Path output = room.locateDirectory("out");
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areNotSameFile();

        // operation
        I.move(input, output);

        // assert contents
        synchrotron.exists(false, true);
    }

    @Test
    public void moveFileToAbsent() throws Exception {
        Path input = room.locateFile("test01/01.txt");
        Path output = room.locateAbsent("out");
        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.exists(true, false);

        // operation
        I.move(input, output);

        // assert contents
        synchrotron.exists(false, true);
    }

    @Test
    public void moveFileToAbsentDeeply() throws Exception {
        Path input = room.locateFile("test01/01.txt");
        Path output = room.locateAbsent("out/put/deeply");
        assert Files.notExists(output.getParent());
        assert Files.notExists(output.getParent().getParent());

        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.exists(true, false);

        // operation
        I.move(input, output);

        // assert contents
        synchrotron.exists(false, true);
    }

    @Test(expected = NoSuchFileException.class)
    public void moveDirectoryToFile() throws Exception {
        Path input = room.locateDirectory("test01");
        Path output = room.locateFile("file");

        // operation
        I.move(input, output);
    }

    @Test
    public void moveDirectoryToDirectory() throws Exception {
        Path input = room.locateDirectory("test01");
        Path output = room.locateDirectory("out");
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.exists(true, false);

        // operation
        I.move(input, output);

        // assert contents
        synchrotron.exists(false, true);
        synchrotron.child("01.txt").exists(false, true);
        synchrotron.sibling("directory1").exists(false, true);
        synchrotron.child("02.txt").exists(false, true);
    }

    @Test
    public void moveDirectoryChildrenToDirectory() throws Exception {
        Path input = room.locateDirectory("test01");
        Path output = room.locateDirectory("out");
        Synchrotron synchrotron = new Synchrotron(input, output);

        // operation
        I.move(input, output, "**");

        // assert contents
        synchrotron.exists(true, true);
        synchrotron.child("01.txt").exists(false, true);
        synchrotron.sibling("directory1").exists(false, true);
        synchrotron.child("02.txt").exists(false, true);
    }

    @Test
    public void moveDirectoryToDirectoryWithReplace() throws Exception {
        Path input = room.locateDirectory("test01");
        Path output = room.locateDirectory("out");
        Files.createFile(output.resolve("01.txt")); // create replaced file
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.exists(true, false);

        // operation
        I.move(input, output);

        // assert contents
        synchrotron.exists(false, true);
        synchrotron.child("01.txt").exists(false, true);
        synchrotron.sibling("directory1").exists(false, true);
        synchrotron.child("02.txt").exists(false, true);
    }

    @Test
    public void moveDirectoryToAbsent() throws Exception {
        Path input = room.locateDirectory("test01");
        Path output = room.locateAbsent("out");

        // operation
        I.move(input, output);

        // assert contents
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.exists(false, true);
        synchrotron.child("01.txt").exists(false, true);
        synchrotron.sibling("directory1").exists(false, true);
        synchrotron.child("02.txt").exists(false, true);
    }

    @Test
    public void moveDirectoryToAbsentDeeply() throws Exception {
        Path input = room.locateDirectory("test01");
        Path output = room.locateAbsent("out/put/deeply");
        assert Files.notExists(output.getParent());
        assert Files.notExists(output.getParent().getParent());

        // operation
        I.move(input, output);

        // assert contents
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.exists(false, true);
        synchrotron.child("01.txt").exists(false, true);
        synchrotron.sibling("directory1").exists(false, true);
        synchrotron.child("02.txt").exists(false, true);
    }

    @Test(expected = NullPointerException.class)
    public void moveNullInput() throws Exception {
        Path input = null;
        Path output = room.locateAbsent("null");

        // operation
        I.move(input, output);
    }

    @Test(expected = NullPointerException.class)
    public void moveNullOutput() throws Exception {
        Path input = room.locateAbsent("null");
        Path output = null;

        // operation
        I.move(input, output);
    }

    @Test
    public void moveAbsentToFile() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateFile("out");

        // operation
        I.move(input, output);
    }

    @Test
    public void moveAbsentToDirectory() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateDirectory("out");

        // operation
        I.move(input, output);
    }

    @Test
    public void moveAbsentToAbsent() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateAbsent("out");

        // operation
        I.move(input, output);
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
        assert !Files.exists(path);
        assert !Files.isDirectory(path);
        assert !Files.isRegularFile(path);
    }

    @Test
    public void createTemporaries() throws Exception {
        Path path1 = I.locateTemporary();
        Path path2 = I.locateTemporary();
        Path path3 = I.locateTemporary();
        assert !Files.exists(path1);
        assert !Files.exists(path2);
        assert !Files.exists(path3);
        assert path1.getFileName() != path2.getFileName();
        assert path3.getFileName() != path2.getFileName();
        assert path3.getFileName() != path1.getFileName();
    }
}
