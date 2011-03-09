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

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * @version 2011/03/07 18:06:48
 */
@RunWith(PowerAssertRunner.class)
public class FilerTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    @Test
    public void copyFileToFile() throws Exception {
        Path input = room.locateFile2("directory/01.txt");
        Path output = room.locateFile2("out");
        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.areNotSameFile();

        // operation
        Filer.copy(input, output);

        // assert contents
        synchrotron.areSameFile();
    }

    @Test
    public void copyFileToDirectory() throws Exception {
        Path input = room.locateFile2("directory/01.txt");
        Path output = room.locateDirectory2("out");
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areNotSameFile();

        // operation
        Filer.copy(input, output);

        // assert contents
        synchrotron.areSameFile();
    }

    @Test
    public void copyFileToAbsent() throws Exception {
        Path input = room.locateFile2("directory/01.txt");
        Path output = room.locateAbsent2("out");
        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.areNotSameFile();

        // operation
        Filer.copy(input, output);

        // assert contents
        synchrotron.areSameFile();
    }

    @Test(expected = NoSuchFileException.class)
    public void copyDirectoryToFile() throws Exception {
        Path input = room.locateDirectory2("directory");
        Path output = room.locateFile2("file");

        // operation
        Filer.copy(input, output);
    }

    @Test
    public void copyDirectoryToDirectory() throws Exception {
        Path input = room.locateDirectory2("directory");
        Path output = room.locateDirectory2("out");
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
        Path input = room.locateDirectory2("directory");
        Path output = room.locateAbsent2("out");

        // operation
        Filer.copy(input, output);

        // assert contents
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areSameDirectory();
        synchrotron.child("01.txt").areSameFile();
        synchrotron.sibling("child").areSameDirectory();
        synchrotron.child("02.txt").areSameFile();
    }

    @Test
    public void moveFileToFile() throws Exception {
        Path input = room.locateFile2("directory/01.txt");
        Path output = room.locateFile2("out");
        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.areNotSameFile();

        // operation
        Filer.move(input, output);

        // assert contents
        synchrotron.exists(false, true);
    }

    @Test
    public void moveFileToDirectory() throws Exception {
        Path input = room.locateFile2("directory/01.txt");
        Path output = room.locateDirectory2("out");
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areNotSameFile();

        // operation
        Filer.move(input, output);

        // assert contents
        synchrotron.exists(false, true);
    }

    @Test
    public void moveFileToAbsent() throws Exception {
        Path input = room.locateFile2("directory/01.txt");
        Path output = room.locateAbsent2("out");
        Synchrotron synchrotron = new Synchrotron(input, output);
        synchrotron.exists(true, false);

        // operation
        Filer.move(input, output);

        // assert contents
        synchrotron.exists(false, true);
    }

    @Test(expected = NoSuchFileException.class)
    public void moveDirectoryToFile() throws Exception {
        Path input = room.locateDirectory2("directory");
        Path output = room.locateFile2("file");

        // operation
        Filer.move(input, output);
    }

    @Test
    public void moveDirectoryToDirectory() throws Exception {
        Path input = room.locateDirectory2("directory");
        Path output = room.locateDirectory2("out");
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
        Path input = room.locateDirectory2("directory");
        Path output = room.locateAbsent2("out");

        // operation
        Filer.move(input, output);

        // assert contents
        Synchrotron synchrotron = new Synchrotron(input, output.resolve(input.getFileName()));
        synchrotron.areNotSameDirectory();
        synchrotron.child("01.txt").areNotSameDirectory();
        synchrotron.sibling("child").areNotSameDirectory();
        synchrotron.child("02.txt").areNotSameDirectory();
    }

    @Test
    public void deleteFile() {
        Path input = room.locateFile2("directory/01.txt");

        assert Files.exists(input);

        // operation
        Filer.delete(input);

        assert Files.notExists(input);
    }

    @Test
    public void deleteDirectory() {
        Path input = room.locateDirectory2("directory");

        assert Files.exists(input);
        assert Files.exists(input.resolve("01.txt"));
        assert Files.exists(input.resolve("child/01.txt"));

        // operation
        Filer.delete(input);

        assert Files.notExists(input);
        assert Files.notExists(input.resolve("01.txt"));
        assert Files.notExists(input.resolve("child/01.txt"));
    }

    @Test
    public void deleteAbsent() {
        Path input = room.locateAbsent2("absent");

        assert Files.notExists(input);

        // operation
        Filer.delete(input);

        assert Files.notExists(input);
    }
}
