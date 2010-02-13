/*
 * Copyright (C) 2010 Nameless Production Committee.
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
package ezbean.io;

import static ezunit.Ezunit.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

import ezunit.CleanRoom;

/**
 * DOCUMENT.
 * 
 * @version 2008/08/26 5:25:31
 */
public class FileSystemUtilityTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    @Test
    public void copyFileToPresentFile() throws Exception {
        File input = room.locateFile("file");
        File output = room.locateAbsent("out");
        output.createNewFile();

        // assert contents
        assertFile(output, "");

        // copy
        FileSystem.copy(input, output);

        // assert contents
        assertFile(output, read(input));
    }

    @Test
    public void copyToAbsentFileInPresentDirectory() throws Exception {
        File input = room.locateFile("file");
        File output = room.locateAbsent("directory/out");

        // copy
        FileSystem.copy(input, output);

        // assert contents
        assertFile(output, read(input));
    }

    @Test
    public void copyFileToAbsentFileInAbsentDirectory() throws Exception {
        File input = room.locateFile("file");
        File output = room.locateAbsent("absent/out");

        // copy
        FileSystem.copy(input, output);

        // assert contents
        assertFile(output, read(input));
    }

    /**
     * Directory copy to present directory which is empty.
     */
    @Test
    public void copyDirectoryToPresentDirectory() throws Exception {
        File input = room.locateDirectory("directory");
        File output = room.locateAbsent("out");
        output.mkdirs();

        FileSystem.copy(input, output);

        // assert contents
        assertFile(new File(output, "directory/file"), "some contents");
        assertDirectory(new File(output, "directory/child"), "child");
        assertFile(new File(output, "directory/child/file"), "some contents");
    }

    /**
     * Directory copy to present directory with filter.
     */
    @Test
    public void copyDirectoryToPresentDirectoryWithFilter() throws Exception {
        File input = room.locateDirectory("directory");
        File output = room.locateAbsent("out");
        output.mkdirs();

        FileSystem.copy(input, output, new FileFilter() {

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
    }

    /**
     * Directory copy to not-present directory.
     */
    @Test
    public void copyDirectoryToAbsentFile() throws Exception {
        File input = room.locateDirectory("directory");
        File output = room.locateAbsent("out");

        FileSystem.copy(input, output);

        // assert contents
        assertFile(new File(output, "directory/file"), "some contents");
        assertDirectory(new File(output, "directory/child"), "child");
        assertFile(new File(output, "directory/child/file"), "some contents");
    }

    /**
     * Directory copy to file.
     */
    @Test(expected = FileNotFoundException.class)
    public void copyDirectoryToPresentFile() throws Exception {
        File input = room.locateDirectory("directory");
        File output = room.locateFile("file");

        FileSystem.copy(input, output);
    }

    /**
     * Input source is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void copyNullInput() throws Exception {
        FileSystem.copy(null, room.locateAbsent("null"));
    }

    /**
     * Input source is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void copyNullOutput() throws Exception {
        FileSystem.copy(room.locateAbsent("null"), null);
    }

    /**
     * Input source is not found.
     */
    @Test(expected = FileNotFoundException.class)
    public void copyAbsentInput() throws Exception {
        FileSystem.copy(room.locateAbsent("absent"), room.locateFile("file"));
    }

    @Test
    public void clearFile() throws Exception {
        File file = room.locateFile("file");

        // assert contents
        assertFile(file, "some contents");

        // clear
        FileSystem.clear(file);

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
        assertDirectory(new File(file, "child"), "child");
        assertFile(new File(file, "child/file"), "some contents");

        // clear
        FileSystem.clear(file);

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
        FileSystem.clear(file);

        assertFalse(file.exists());
    }

    @Test
    public void clearNull() {
        FileSystem.clear(null); // no error
    }

    @Test
    public void deleteFile() throws Exception {
        File file = room.locateFile("file");

        // assert contents
        assertFile(file, "some contents");

        // delete
        FileSystem.delete(file);

        // assert contents
        assertFalse(file.exists());
    }

    @Test
    public void deleteDirectory() throws Exception {
        File file = room.locateDirectory("directory");

        // assert contents
        assertFile(new File(file, "file"), "some contents");
        assertDirectory(new File(file, "child"), "child");
        assertFile(new File(file, "child/file"), "some contents");

        // delete
        FileSystem.delete(file);

        // assert contents
        assertFalse(file.exists());
        assertFalse(new File(file, "file").exists());
        assertFalse(new File(file, "child").exists());
        assertFalse(new File(file, "child/file").exists());
    }

    @Test
    public void deleteArchive() throws Exception {
        File file = room.locateFile("archive.zip");

        assertTrue(file instanceof ezbean.io.File);
        ezbean.io.File archive = (ezbean.io.File) file;
        File junction = archive.getJunction();

        // unpack
        archive.list();
        assertTrue(junction.exists());

        // delete
        FileSystem.delete(file);

        assertFalse(file.exists());
        assertFalse(junction.exists());
    }

    @Test
    public void deleteAbsent() throws Exception {
        File file = room.locateAbsent("absent");

        // delete
        FileSystem.delete(file);

        // assert contents
        assertFalse(file.exists());
    }

    @Test
    public void deleteNull() {
        assertFalse(FileSystem.delete(null)); // no error
    }

    @Test
    public void fileEquality() throws Exception {
        File one = new File("a");
        File other = new File("a/../a");

        assertFalse(one.equals(other));
        assertFalse(other.equals(one));
    }

    /**
     * Retrieve file name.
     */
    @Test
    public void testGetFileName() {
        assertEquals("", FileSystem.getName(null));
        assertEquals("name", FileSystem.getName(new File("name")));
        assertEquals("name", FileSystem.getName(new File("name.")));
        assertEquals("name.and", FileSystem.getName(new File("name.and.extension")));
        assertEquals("name", FileSystem.getName(new File("directory/name")));
        assertEquals("name", FileSystem.getName(new File("directory/name.extension")));
    }

    /**
     * Retrieve file extension.
     */
    @Test
    public void testGetFileExtension() {
        assertEquals("", FileSystem.getExtension(null));
        assertEquals("", FileSystem.getExtension(new File("name")));
        assertEquals("", FileSystem.getExtension(new File("name.")));
        assertEquals("extension", FileSystem.getExtension(new File("name.extension")));
        assertEquals("extension", FileSystem.getExtension(new File("name.dummy.extension")));
        assertEquals("", FileSystem.getExtension(new File("directory/name")));
        assertEquals("extension", FileSystem.getExtension(new File("directory/name.extension")));
    }

    /**
     * Create temporary file.
     */
    @Test
    public void testCreateTemporaryAsFile() throws IOException {
        File file = FileSystem.createTemporary();
        assertFalse(file.exists());
        assertTrue(file.createNewFile());
    }

    /**
     * Create temporary directory.
     */
    @Test
    public void testCreateTemporaryAsDirectory() throws IOException {
        File file = FileSystem.createTemporary();
        assertFalse(file.exists());
        assertTrue(file.mkdir());
    }

    @Test
    public void dontCreateDuplicatedName() {
        File file1 = FileSystem.createTemporary();
        File file2 = FileSystem.createTemporary();
        assertNotSame(file1.getName(), file2.getName());
    }
}
