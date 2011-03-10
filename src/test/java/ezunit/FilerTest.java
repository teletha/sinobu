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
package ezunit;

import static org.junit.Assert.*;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2011/03/07 18:06:48
 */
public class FilerTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    @Test
    public void copyFileToFile() throws Exception {
        Path input = room.locateFile("directory/01.txt");
        Path output = room.locateFile("out");
        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.areNotSameFile();

        // operation
        Filer.copy(input, output);

        // assert contents
        synchrotron.areSameFile();
    }

    @Test
    public void copyFileToDirectory() throws Exception {
        Path input = room.locateFile("directory/01.txt");
        Path output = room.locateDirectory("out");
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areNotSameFile();

        // operation
        Filer.copy(input, output);

        // assert contents
        synchrotron.areSameFile();
    }

    @Test
    public void copyFileToAbsent() throws Exception {
        Path input = room.locateFile("directory/01.txt");
        Path output = room.locateAbsent("out");
        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.areNotSameFile();

        // operation
        Filer.copy(input, output);

        // assert contents
        synchrotron.areSameFile();
    }

    @Test(expected = NoSuchFileException.class)
    public void copyDirectoryToFile() throws Exception {
        Path input = room.locateDirectory("directory");
        Path output = room.locateFile("file");

        // operation
        Filer.copy(input, output);
    }

    @Test
    public void copyDirectoryToDirectory() throws Exception {
        Path input = room.locateDirectory("directory");
        Path output = room.locateDirectory("out");
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areNotSameDirectory();

        // operation
        Filer.copy(input, output);

        // assert contents
        synchrotron.areSameDirectory();
        synchrotron.child("01.txt").areSameFile();
        synchrotron.sibling("child").areSameDirectory();
        synchrotron.child("02.txt").areSameFile();
    }

    @Test
    public void copyDirectoryToAbsent() throws Exception {
        Path input = room.locateDirectory("directory");
        Path output = room.locateAbsent("out");

        // operation
        Filer.copy(input, output);

        // assert contents
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areSameDirectory();
        synchrotron.child("01.txt").areSameFile();
        synchrotron.sibling("child").areSameDirectory();
        synchrotron.child("02.txt").areSameFile();
    }

    @Test(expected = NullPointerException.class)
    public void copyNullInput() throws Exception {
        Path input = null;
        Path output = room.locateAbsent("null");

        // operation
        Filer.copy(input, output);
    }

    @Test(expected = NullPointerException.class)
    public void copyNullOutput() throws Exception {
        Path input = room.locateAbsent("null");
        Path output = null;

        // operation
        Filer.copy(input, output);
    }

    @Test(expected = NoSuchFileException.class)
    public void copyAbsentToFile() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateFile("out");

        // operation
        Filer.copy(input, output);
    }

    @Test(expected = NoSuchFileException.class)
    public void copyAbsentToDirectory() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateDirectory("out");

        // operation
        Filer.copy(input, output);
    }

    @Test(expected = NoSuchFileException.class)
    public void copyAbsentToAbsent() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateAbsent("out");

        // operation
        Filer.copy(input, output);
    }

    @Test
    public void moveFileToFile() throws Exception {
        Path input = room.locateFile("directory/01.txt");
        Path output = room.locateFile("out");
        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.areNotSameFile();

        // operation
        Filer.move(input, output);

        // assert contents
        synchrotron.exists(false, true);
    }

    @Test
    public void moveFileToDirectory() throws Exception {
        Path input = room.locateFile("directory/01.txt");
        Path output = room.locateDirectory("out");
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areNotSameFile();

        // operation
        Filer.move(input, output);

        // assert contents
        synchrotron.exists(false, true);
    }

    @Test
    public void moveFileToAbsent() throws Exception {
        Path input = room.locateFile("directory/01.txt");
        Path output = room.locateAbsent("out");
        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.exists(true, false);

        // operation
        Filer.move(input, output);

        // assert contents
        synchrotron.exists(false, true);
    }

    @Test(expected = NoSuchFileException.class)
    public void moveDirectoryToFile() throws Exception {
        Path input = room.locateDirectory("directory");
        Path output = room.locateFile("file");

        // operation
        Filer.move(input, output);
    }

    @Test
    public void moveDirectoryToDirectory() throws Exception {
        Path input = room.locateDirectory("directory");
        Path output = room.locateDirectory("out");
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areNotSameDirectory();

        // operation
        Filer.move(input, output);

        // assert contents
        synchrotron.areNotSameDirectory();
        synchrotron.child("01.txt").areNotSameFile();
        synchrotron.sibling("child").areNotSameFile();
        synchrotron.child("02.txt").areNotSameFile();
    }

    @Test
    public void moveDirectoryToAbsent() throws Exception {
        Path input = room.locateDirectory("directory");
        Path output = room.locateAbsent("out");

        // operation
        Filer.move(input, output);

        // assert contents
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areNotSameDirectory();
        synchrotron.child("01.txt").areNotSameDirectory();
        synchrotron.sibling("child").areNotSameDirectory();
        synchrotron.child("02.txt").areNotSameDirectory();
    }

    @Test(expected = NullPointerException.class)
    public void moveNullInput() throws Exception {
        Path input = null;
        Path output = room.locateAbsent("null");

        // operation
        Filer.move(input, output);
    }

    @Test(expected = NullPointerException.class)
    public void moveNullOutput() throws Exception {
        Path input = room.locateAbsent("null");
        Path output = null;

        // operation
        Filer.move(input, output);
    }

    @Test(expected = NoSuchFileException.class)
    public void moveAbsentToFile() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateFile("out");

        // operation
        Filer.move(input, output);
    }

    @Test(expected = NoSuchFileException.class)
    public void moveAbsentToDirectory() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateDirectory("out");

        // operation
        Filer.move(input, output);
    }

    @Test(expected = NoSuchFileException.class)
    public void moveAbsentToAbsent() throws Exception {
        Path input = room.locateAbsent("absent");
        Path output = room.locateAbsent("out");

        // operation
        Filer.move(input, output);
    }

    @Test
    public void deleteFile() {
        Path input = room.locateFile("directory/01.txt");

        assertTrue(Files.exists(input));

        // operation
        Filer.delete(input);

        assertTrue(Files.notExists(input));
    }

    @Test
    public void deleteDirectory() {
        Path input = room.locateDirectory("directory");

        assertTrue(Files.exists(input));
        assertTrue(Files.exists(input.resolve("01.txt")));
        assertTrue(Files.exists(input.resolve("child/01.txt")));

        // operation
        Filer.delete(input);

        assertTrue(Files.notExists(input));
        assertTrue(Files.notExists(input.resolve("01.txt")));
        assertTrue(Files.notExists(input.resolve("child/01.txt")));
    }

    @Test
    public void deleteAbsent() {
        Path input = room.locateAbsent("absent");

        assertTrue(Files.notExists(input));

        // operation
        Filer.delete(input);

        assertTrue(Files.notExists(input));
    }

    @Test
    public void deleteNull() throws Exception {
        Filer.delete(null);
    }

    @Test
    public void createTemporary() throws Exception {
        Path path = Filer.createTemporary();
        assertFalse(Files.exists(path));
        assertFalse(Files.isDirectory(path));
        assertFalse(Files.isRegularFile(path));
    }

    @Test
    public void createTemporaries() throws Exception {
        Path path1 = Filer.createTemporary();
        Path path2 = Filer.createTemporary();
        Path path3 = Filer.createTemporary();
        assertFalse(Files.exists(path1));
        assertFalse(Files.exists(path2));
        assertFalse(Files.exists(path3));
        assertNotSame(path1.getFileName(), path2.getFileName());
        assertNotSame(path3.getFileName(), path2.getFileName());
        assertNotSame(path3.getFileName(), path1.getFileName());
    }
}
