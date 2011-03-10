/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2011/03/08 18:35:36
 */
public class CleanRoomTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    @Test
    public void locateFile() {
        Path file = room.locateFile("empty");

        assertTrue(Files.exists(file));
        assertTrue(Files.isRegularFile(file));
    }

    @Test
    public void locateArchive() {
        Path file = room.locateFile("archive.zip");

        assertTrue(Files.exists(file));
        assertTrue(Files.isRegularFile(file));
        assertFalse(Files.isDirectory(file));
    }

    @Test
    public void locateDirectoryFromAbsent() {
        Path file = room.locateDirectory("absent");

        assertTrue(Files.exists(file));
        assertTrue(Files.isDirectory(file));
    }

    @Test
    public void locateDirectoryFromPresent() {
        Path file = room.locateDirectory("dir");

        assertTrue(Files.exists(file));
        assertTrue(Files.isDirectory(file));
    }

    @Test
    public void locateAbsent() {
        Path file = room.locateAbsent("absent.txt");

        assertFalse(Files.exists(file));
        assertFalse(Files.isRegularFile(file));
        assertFalse(Files.isDirectory(file));
    }

    @Test
    public void locatePresentFile() {
        Path file = room.locateAbsent("present.txt");

        // the specified file doesn't exist yet
        assertFalse(Files.exists(file));

        // create file
        file = room.locateFile("present.txt");
        assertTrue(Files.exists(file));

        // the file has already existed
        file = room.locateFile("present.txt");
        assertTrue(Files.exists(file));
    }

    @Test
    public void locatedFileCanDelete() throws Exception {
        Path file = room.locateFile("empty");

        assertTrue(Files.exists(file));
        assertTrue(Files.deleteIfExists(file));
        assertFalse(Files.exists(file));
    }

}
