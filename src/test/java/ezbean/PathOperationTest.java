/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;

import ezunit.CleanRoom;
import ezunit.Synchrotron;

/**
 * @version 2011/03/07 18:06:48
 */
public class PathOperationTest {

    @Rule
    public static final CleanRoom room = new CleanRoom("file");

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
        assertTrue(Files.notExists(output.getParent()));
        assertTrue(Files.notExists(output.getParent().getParent()));

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
        assertTrue(Files.notExists(output.getParent()));
        assertTrue(Files.notExists(output.getParent().getParent()));

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

    @Test(expected = NoSuchFileException.class)
    public void copyAbsentToFile() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateFile("out");

        // operation
        I.copy(input, output);
    }

    @Test(expected = NoSuchFileException.class)
    public void copyAbsentToDirectory() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateDirectory("out");

        // operation
        I.copy(input, output);
    }

    @Test(expected = NoSuchFileException.class)
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
        assertTrue(Files.notExists(output.getParent()));
        assertTrue(Files.notExists(output.getParent().getParent()));

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
        assertTrue(Files.notExists(output.getParent()));
        assertTrue(Files.notExists(output.getParent().getParent()));

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

    @Test(expected = NoSuchFileException.class)
    public void moveAbsentToFile() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateFile("out");

        // operation
        I.move(input, output);
    }

    @Test(expected = NoSuchFileException.class)
    public void moveAbsentToDirectory() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateDirectory("out");

        // operation
        I.move(input, output);
    }

    @Test(expected = NoSuchFileException.class)
    public void moveAbsentToAbsent() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateAbsent("out");

        // operation
        I.move(input, output);
    }

    @Test
    public void deleteFile() {
        Path input = room.locateFile("test01/01.txt");

        assertTrue(Files.exists(input));

        // operation
        I.delete(input);

        assertTrue(Files.notExists(input));
    }

    @Test
    public void deleteDirectory() {
        Path input = room.locateDirectory("test01");

        assertTrue(Files.exists(input));
        assertTrue(Files.exists(input.resolve("01.txt")));
        assertTrue(Files.exists(input.resolve("directory1/01.txt")));

        // operation
        I.delete(input);

        assertTrue(Files.notExists(input));
        assertTrue(Files.notExists(input.resolve("01.txt")));
        assertTrue(Files.notExists(input.resolve("directory1/01.txt")));
    }

    @Test
    public void deleteAbsent() {
        Path input = room.locateAbsent("absent");

        assertTrue(Files.notExists(input));

        // operation
        I.delete(input);

        assertTrue(Files.notExists(input));
    }

    @Test
    public void deleteNull() throws Exception {
        I.delete(null);
    }

    @Test
    public void createTemporary() throws Exception {
        Path path = I.locateTemporary();
        assertFalse(Files.exists(path));
        assertFalse(Files.isDirectory(path));
        assertFalse(Files.isRegularFile(path));
    }

    @Test
    public void createTemporaries() throws Exception {
        Path path1 = I.locateTemporary();
        Path path2 = I.locateTemporary();
        Path path3 = I.locateTemporary();
        assertFalse(Files.exists(path1));
        assertFalse(Files.exists(path2));
        assertFalse(Files.exists(path3));
        assertNotSame(path1.getFileName(), path2.getFileName());
        assertNotSame(path3.getFileName(), path2.getFileName());
        assertNotSame(path3.getFileName(), path1.getFileName());
    }
}
