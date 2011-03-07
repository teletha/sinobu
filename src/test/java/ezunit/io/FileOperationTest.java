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
package ezunit.io;

import static ezunit.Ezunit.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

import ezbean.I;
import ezbean.io.FilePath;
import ezunit.CleanRoom;

/**
 * DOCUMENT.
 * 
 * @version 2008/08/26 5:25:31
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
        I.copy(input, output);

        // assert contents
        assertFile(output, read(input));
        assertEquals(input.lastModified(), output.lastModified());
    }

    @Test
    public void copyToAbsentFileInPresentDirectory() throws Exception {
        File input = room.locateFile("file");
        File output = room.locateAbsent("directory/out");

        // copy
        I.copy(input, output);

        // assert contents
        assertFile(output, read(input));
    }

    @Test
    public void copyFileToAbsentFileInAbsentDirectory() throws Exception {
        File input = room.locateFile("file");
        File output = room.locateAbsent("absent/out");

        assertNotSame(input.lastModified(), output.lastModified());

        // copy
        I.copy(input, output);

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

        I.copy(input, output);

        // assert contents
        assertFile(new File(output, "directory/1"), "1");
        assertDirectory(new File(output, "directory/child"));
        assertFile(new File(output, "directory/child/a"), "a");
        assertEquals(input.lastModified(), new File(output, "directory").lastModified());
    }

    /**
     * Directory copy to present directory with filter.
     */
    @Test
    public void copyDirectoryToPresentDirectoryWithFilter() throws Exception {
        File input = room.locateDirectory("directory");
        File output = room.locateAbsent("out");
        output.mkdirs();

        I.copy(input, output, new FileFilter() {

            /**
             * @see java.io.FileFilter#accept(java.io.File)
             */
            public boolean accept(File file) {
                return file.isFile();
            }
        });

        // assert contents
        assertFile(new File(output, "directory/file"), "some contents");
        assertFalse(new File(output, "directory/child").exists());
        assertEquals(input.lastModified(), new File(output, "directory").lastModified());
    }

    /**
     * Directory copy to not-present directory.
     */
    @Test
    public void copyDirectoryToAbsentFile() throws Exception {
        File input = room.locateDirectory("directory");
        File output = room.locateAbsent("out");

        I.copy(input, output);

        // assert contents
        assertFile(new File(output, "directory/2"), "2");
        assertDirectory(new File(output, "directory/child"));
        assertFile(new File(output, "directory/child/b"), "b");
        assertEquals(input.lastModified(), new File(output, "directory").lastModified());
    }

    /**
     * Directory copy to file.
     */
    @Test(expected = FileNotFoundException.class)
    public void copyDirectoryToPresentFile() throws Exception {
        File input = room.locateDirectory("directory");
        File output = room.locateFile("file");

        I.copy(input, output, null);
    }

    /**
     * Input source is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void copyNullInput() throws Exception {
        I.copy(null, room.locateAbsent("null"));
    }

    /**
     * Input source is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void copyNullOutput() throws Exception {
        I.copy(room.locateAbsent("null"), null);
    }

    /**
     * Input source is not found.
     */
    @Test
    public void copyAbsentInput() throws Exception {
        I.copy(room.locateAbsent("absent"), room.locateFile("file"));
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

    // @Test
    // public void deleteArchive() throws Exception {
    // File file = room.locateFile("archive/test.zip");
    //
    // assertTrue(file instanceof ezbean.io.FilePath);
    // ezbean.io.FilePath archive = (ezbean.io.FilePath) file;
    // File junction = archive.getJunction();
    //
    // // unpack
    // archive.list();
    // assertTrue(junction.exists());
    //
    // // delete
    // I.delete(file);
    //
    // assertFalse(file.exists());
    // assertFalse(junction.exists());
    // }

    @Test
    public void deleteAbsent() throws Exception {
        File file = room.locateAbsent("absent");

        // delete
        file.delete();

        // assert contents
        assertFalse(file.exists());
    }

    @Test
    public void fileEquality() throws Exception {
        File one = new File("a");
        File other = new File("a/../a");

        assertFalse(one.equals(other));
        assertFalse(other.equals(one));
    }

    /**
     * Create temporary file.
     */
    @Test
    public void testCreateTemporaryAsFile() throws IOException {
        File file = FilePath.createTemporary();
        assertFalse(file.exists());
        assertTrue(file.createNewFile());
    }

    /**
     * Create temporary directory.
     */
    @Test
    public void testCreateTemporaryAsDirectory() throws IOException {
        File file = FilePath.createTemporary();
        assertFalse(file.exists());
        assertTrue(file.mkdir());
    }

    @Test
    public void dontCreateDuplicatedName() {
        File file1 = FilePath.createTemporary();
        File file2 = FilePath.createTemporary();
        assertNotSame(file1.getName(), file2.getName());
    }
}
