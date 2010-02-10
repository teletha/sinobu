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

import static ezbean.unit.Ezunit.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

import ezbean.unit.CleanRoom;

/**
 * DOCUMENT.
 * 
 * @version 2008/08/26 5:25:31
 */
public class FileSystemUtilityTest extends FileSystemTestCase {

    @Rule
    public static final CleanRoom room = new CleanRoom("src/test/resources/ezbean/io");

    @Test
    public void copyToPresentFile() throws Exception {
        File input = room.locateFile("file");
        File output = room.newPresentFile("file");

        // empty file
        assertFile(output, "file", "");

        // copy
        FileSystem.copy(input, output);

        // assert contents
        assertFile(output, "file", "some contents");
    }

    @Test
    public void copyToAbsentFile() throws Exception {
        File input = room.locateFile("file");
        File output = room.newAbsentFile("file");

        // copy
        FileSystem.copy(input, output);

        // assert contents
        assertFile(output, "file", "some contents");
    }

    /**
     * File copy to present directory which has same name file.
     */
    @Test
    public void testCopy03() throws Exception {
        File input = createResourceFile("file");
        createPresentTestDirectory("test");
        File output = createPresentTestFile("test/file");

        FileSystem.copy(input, output);

        // assert contents
        assertFile(output, "file", "some contents");
    }

    /**
     * File copy to present directory which is empty.
     */
    @Test
    public void testCopy04() throws Exception {
        File input = createResourceFile("file");
        createPresentTestDirectory("test");
        File output = createAbsentTestFile("test/file");

        FileSystem.copy(input, output);

        // assert contents
        assertFile(output, "file", "some contents");
    }

    /**
     * File copy to non-present directory.
     */
    @Test
    public void testCopy05() throws Exception {
        File input = createResourceFile("file");
        createAbsentTestDirectory("test");
        File output = createAbsentTestFile("test/file");

        FileSystem.copy(input, output);

        // assert contents
        assertFile(output, "file", "some contents");
    }

    /**
     * Directory copy to present directory which is empty.
     */
    @Test
    public void testCopy06() throws Exception {
        File input = createResourceDirectory("directory");
        File output = createPresentTestDirectory("test");

        FileSystem.copy(input, output);

        // assert contents
        assertFile(new File(output, "directory/file"), "file", "some contents");
        assertDirectory(new File(output, "directory/child"), "child");
        assertFile(new File(output, "directory/child/file"), "file", "some contents");
    }

    /**
     * Directory copy to present directory with filter.
     */
    @Test
    public void testCopy07() throws Exception {
        File input = createResourceDirectory("directory");
        File output = createPresentTestDirectory("test");

        FileSystem.copy(input, output, new FileFilter() {

            /**
             * @see java.io.FileFilter#accept(java.io.File)
             */
            public boolean accept(File file) {
                return file.isFile();
            }
        });

        // assert contents
        assertFile(new File(output, "directory/file"), "file", "some contents");
        assertFalse(new File(output, "directory/child").exists());
    }

    /**
     * Directory copy to not-present directory.
     */
    @Test
    public void testCopy08() throws Exception {
        File input = createResourceDirectory("directory");
        File output = createAbsentTestDirectory("test");

        FileSystem.copy(input, output);

        // assert contents
        assertFile(new File(output, "directory/file"), "file", "some contents");
        assertDirectory(new File(output, "directory/child"), "child");
        assertFile(new File(output, "directory/child/file"), "file", "some contents");
    }

    /**
     * Directory copy to file.
     */
    @Test(expected = FileNotFoundException.class)
    public void testCopy09() throws Exception {
        File input = createResourceDirectory("directory");
        File output = createPresentTestFile("test");

        FileSystem.copy(input, output);
    }

    /**
     * Input source is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void testCopyNullInput() throws Exception {
        FileSystem.copy(null, createAbsentTestFile("none"));
    }

    /**
     * Input source is <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void testCopyNullOutput() throws Exception {
        FileSystem.copy(createResourceFile("file"), null);
    }

    /**
     * Input source is not found.
     */
    @Test(expected = FileNotFoundException.class)
    public void testCopyNotPresentInput() throws Exception {
        FileSystem.copy(createResourceFileWithNoAssert("not-found"), createPresentTestFile("found"));
    }

    /**
     * Test method for {@link ezbean.io.FileSystem#copy(java.io.InputStream, java.io.OutputStream)}.
     */
    @Test
    public void testCopyInputStreamOutputStream() {

    }

    /**
     * Clear file.
     */
    @Test
    public void testClear01() throws Exception {
        File input = createResourceFile("file");
        File output = createPresentTestFile("file");

        // copy
        FileSystem.copy(input, output);

        // assert contents
        assertFile(output, "file", "some contents");

        // clear
        FileSystem.clear(output);

        // assert contents
        assertFile(output, "file", null);
    }

    /**
     * Clear directory.
     */
    @Test
    public void testClear02() throws Exception {
        File input = createResourceDirectory("directory");
        File output = createPresentTestDirectory("test");

        // copy
        FileSystem.copy(input, output);

        // assert contents
        assertFile(new File(output, "directory/file"), "file", "some contents");
        assertDirectory(new File(output, "directory/child"), "child");
        assertFile(new File(output, "directory/child/file"), "file", "some contents");

        // clear
        FileSystem.clear(output);

        // assert contents
        assertTrue(output.exists());
        assertFalse(new File(output, "directory").exists());
        assertFalse(new File(output, "directory/file").exists());
        assertFalse(new File(output, "directory/child").exists());
        assertFalse(new File(output, "directory/child/file").exists());
    }

    /**
     * Clear with <code>null</code> input.
     */
    @Test
    public void testClearNull() {
        FileSystem.clear(null); // no error
    }

    /**
     * Delete file.
     */
    @Test
    public void testDeleteFile() throws Exception {
        File input = createResourceFile("file");
        File output = createPresentTestFile("file");

        // copy
        FileSystem.copy(input, output);

        // assert contents
        assertFile(output, "file", "some contents");

        // delete
        FileSystem.delete(output);

        // assert contents
        assertFalse(output.exists());
    }

    /**
     * Delete directory.
     */
    @Test
    public void testDeleteDirectory() throws Exception {
        File input = createResourceDirectory("directory");
        File output = createPresentTestDirectory("test");

        // copy
        FileSystem.copy(input, output);

        // assert contents
        assertFile(new File(output, "directory/file"), "file", "some contents");
        assertDirectory(new File(output, "directory/child"), "child");
        assertFile(new File(output, "directory/child/file"), "file", "some contents");

        // delete
        FileSystem.delete(output);

        // assert contents
        assertFalse(output.exists());
        assertFalse(new File(output, "directory").exists());
        assertFalse(new File(output, "directory/file").exists());
        assertFalse(new File(output, "directory/child").exists());
        assertFalse(new File(output, "directory/child/file").exists());
    }

    /**
     * Delete archive.
     */
    @Test
    public void testDeleteArchive() throws Exception {
        File input = createResourceFile("archive.zip");
        File output = createPresentTestFile("archive.zip");

        // copy
        FileSystem.copy(input, output);

        // delete
        FileSystem.delete(output);

        // assert contents
        assertFalse(output.exists());
    }

    /**
     * Delete with <code>null</code> input.
     */
    @Test
    public void testDeleteNull() {
        assertFalse(FileSystem.delete(null)); // no error
    }

    // /**
    // * Test null and null.
    // */
    // @Test
    // public void testEquals1() throws Exception {
    // File one = null;
    // File other = null;
    //
    // assertTrue(FileSystem.equals(one, other));
    // }
    //
    // /**
    // * Test null and A.
    // */
    // @Test
    // public void testEquals2() throws Exception {
    // File one = null;
    // File other = new File("");
    //
    // assertFalse(FileSystem.equals(one, other));
    // assertFalse(FileSystem.equals(other, one));
    // }
    //
    // /**
    // * Test A and A.
    // */
    // @Test
    // public void testEquals4() throws Exception {
    // File one = new File("");
    // File other = new File("");
    //
    // assertTrue(FileSystem.equals(one, other));
    // }
    //
    // /**
    // * Test A and B.
    // */
    // @Test
    // public void testEquals5() throws Exception {
    // File one = new File("a");
    // File other = new File("b");
    //
    // assertFalse(FileSystem.equals(one, other));
    // assertFalse(FileSystem.equals(other, one));
    // }
    //
    // /**
    // * Test A and A.
    // */
    // @Test
    // public void testEquals6() throws Exception {
    // File one = new File("a");
    // File other = new File("a").getAbsoluteFile();
    //
    // assertTrue(FileSystem.equals(one, other));
    // assertTrue(FileSystem.equals(other, one));
    // }
    //
    // /**
    // * Test A and A.
    // */
    // @Test
    // public void testEquals7() throws Exception {
    // File one = new File("a");
    // File other = new File("a/../a");
    //
    // assertTrue(FileSystem.equals(one, other));
    // assertTrue(FileSystem.equals(other, one));
    // }

    /**
     * Test A and A.
     */
    @Test
    public void testEquals8() throws Exception {
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
