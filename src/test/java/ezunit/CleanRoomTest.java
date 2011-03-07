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

import java.io.File;

import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2010/02/11 13:15:37
 */
public class CleanRoomTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    @Test
    public void locateFile() {
        ezunit.io.File file = room.locateFile2("empty");

        assertTrue(file.exists());
        assertTrue(file.isFile());
    }

    @Test
    public void locateArchive() {
        ezunit.io.File file = room.locateFile2("archive.zip");

        assertTrue(file.exists());
        assertTrue(file.isFile());
        assertFalse(file.isDirectory());
    }

    @Test
    public void locateDirectoryFromAbsent() {
        ezunit.io.File file = room.locateDirectory2("absent");

        assertTrue(file.exists());
        assertTrue(file.isDirectory());
    }

    @Test
    public void locateDirectoryFromPresent() {
        ezunit.io.File file = room.locateDirectory2("dir");

        assertTrue(file.exists());
        assertTrue(file.isDirectory());
    }

    @Test
    public void locateAbsentFile() {
        File file = room.locateAbsent("absent.txt");

        assertFalse(file.exists());
        assertFalse(file.isFile());
        assertFalse(file.isDirectory());
    }

    @Test
    public void locateAbsentArchive() {
        File file = room.locateAbsent("absent");

        assertFalse(file.exists());
        assertFalse(file.isFile());
        assertFalse(file.isDirectory());
    }

    @Test
    public void locateAbsentDirectory() {
        File file = room.locateAbsent("absent.zip");

        assertFalse(file.exists());
        assertFalse(file.isFile());
        assertFalse(file.isDirectory());
    }

    @Test
    public void locatePresentFile() {
        File file = room.locateAbsent("present.txt");

        // the specified file doesn't exist yet
        assertFalse(file.exists());

        // create file
        file = room.locateFile("present.txt");
        assertTrue(file.exists());

        // the file has already existed
        file = room.locateFile("present.txt");
        assertTrue(file.exists());
    }

    @Test
    public void locatedFileCanDelete() {
        File file = room.locateFile("empty");

        assertTrue(file.exists());
        assertTrue(file.delete());
        assertFalse(file.exists());
    }

}
