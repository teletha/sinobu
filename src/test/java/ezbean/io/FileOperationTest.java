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
package ezbean.io;

import static ezunit.Ezunit.*;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.NoSuchFileException;

import org.junit.Rule;
import org.junit.Test;

import ezunit.CleanRoom;

/**
 * @version 2011/02/25 17:56:12
 */
public class FileOperationTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    @Test
    public void copyFileToPresentFile() throws Exception {
        File input = room.locateFile("file");
        File output = room.locateAbsent("out");
        output.createNewFile();

        // assert contents
        assertFile(output, "");
        assertNotSame(input.lastModified(), output.lastModified());

        // copy
        ((ezbean.io.File) input).copyTo(output);

        // assert contents
        assertFile(output, read(input));
        assertEquals(input.lastModified(), output.lastModified());
    }

    @Test
    public void copyToAbsentFileInPresentDirectory() throws Exception {
        File input = room.locateFile("file");
        File output = room.locateAbsent("directory/out");

        // copy
        ((ezbean.io.File) input).copyTo(output);

        // assert contents
        assertFile(output, read(input));
    }

    @Test
    public void copyFileToAbsentFileInAbsentDirectory() throws Exception {
        File input = room.locateFile("file");
        File output = room.locateAbsent("absent/out");

        assertNotSame(input.lastModified(), output.lastModified());

        // copy
        ((ezbean.io.File) input).copyTo(output);

        // assert contents
        assertFile(output, read(input));
        assertEquals(input.lastModified(), output.lastModified());
    }

    /**
     * Directory copy to present directory which is empty.
     */
    @Test
    public void copyDirectoryToPresentDirectory() throws Exception {
        File input = room.locateDirectory("directory");
        File output = room.locateAbsent("out");
        output.mkdirs();

        ((ezbean.io.File) input).copyTo(output);

        // assert contents
        assertFile(new File(output, "directory/1"), "1");
        assertDirectory(new File(output, "directory/child"));
        assertFile(new File(output, "directory/child/a"), "a");
        assertEquals(input.lastModified(), new File(output, "directory").lastModified());
    }

    /**
     * Directory copy to not-present directory.
     */
    @Test
    public void copyDirectoryToAbsentFile() throws Exception {
        File input = room.locateDirectory("directory");
        File output = room.locateAbsent("out");

        ((ezbean.io.File) input).copyTo(output);

        // assert contents
        assertFile(new File(output, "directory/2"), "2");
        assertDirectory(new File(output, "directory/child"));
        assertFile(new File(output, "directory/child/b"), "b");
        assertEquals(input.lastModified(), new File(output, "directory").lastModified());
    }

    /**
     * Directory copy to file.
     */
    @Test(expected = NoSuchFileException.class)
    public void copyDirectoryToPresentFile() throws Exception {
        File input = room.locateDirectory("directory");
        File output = room.locateFile("file");

        ((ezbean.io.File) input).copyTo(output);
    }

    /**
     * Input source is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void copyNullOutput() throws Exception {
        ((ezbean.io.File) room.locateAbsent("null")).copyTo(null);
    }

    /**
     * Input source is not found.
     */
    @Test(expected = NoSuchFileException.class)
    public void copyAbsentInput() throws Exception {
        ((ezbean.io.File) room.locateAbsent("absent")).copyTo(room.locateFile("file"));
    }

    @Test
    public void clearFile() throws Exception {
        File file = room.locateFile("file");

        // assert contents
        assertFile(file, "some contents");

        // clear
        ((ezbean.io.File) file).clear();

        // assert contents
        assertFile(file, "");
    }

    /**
     * Clear directory.
     */
    @Test
    public void clearDirectory() throws Exception {
        File file = room.locateDirectory("directory");

        // assert contents
        assertFile(new File(file, "file"), "some contents");
        assertDirectory(new File(file, "child"));
        assertFile(new File(file, "child/c"), "c");

        // clear
        ((ezbean.io.File) file).clear();

        // assert contents
        assertTrue(file.exists());
        assertFalse(new File(file, "file").exists());
        assertFalse(new File(file, "child").exists());
        assertFalse(new File(file, "child/file").exists());
    }

    @Test
    public void clearAbsent() throws Exception {
        File file = room.locateAbsent("absent");

        assertFalse(file.exists());

        // clear
        ((ezbean.io.File) file).clear();

        assertFalse(file.exists());
    }

    @Test
    public void deleteFile() throws Exception {
        File file = room.locateFile("file");

        // assert contents
        assertFile(file, "some contents");

        // delete
        file.delete();

        // assert contents
        assertFalse(file.exists());
    }

    @Test
    public void deleteDirectory() throws Exception {
        File file = room.locateDirectory("directory");

        // assert contents
        assertFile(new File(file, "file"), "some contents");
        assertDirectory(new File(file, "child"));
        assertFile(new File(file, "child/a"), "a");

        // delete
        file.delete();

        // assert contents
        assertFalse(file.exists());
        assertFalse(new File(file, "file").exists());
        assertFalse(new File(file, "child").exists());
        assertFalse(new File(file, "child/file").exists());
    }

    @Test
    public void deleteArchive() throws Exception {
        File file = room.locateFile("archive/test.zip");

        assertTrue(file instanceof ezbean.io.File);
        ezbean.io.File archive = (ezbean.io.File) file;
        File junction = archive.getJunction();

        // unpack
        archive.list();
        assertTrue(junction.exists());

        // delete
        file.delete();

        assertFalse(file.exists());
        assertFalse(junction.exists());
    }

    @Test
    public void deleteAbsent() throws Exception {
        File file = room.locateAbsent("absent");

        // delete
        file.delete();

        // assert contents
        assertFalse(file.exists());
    }

}
