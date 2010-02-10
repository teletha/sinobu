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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.zip.ZipEntry;

import org.junit.Test;

import ezbean.I;

/**
 * DOCUMENT.
 * 
 * @version 2008/06/18 9:17:52
 */
public class FileTest extends FileSystemTestCase {

    private static final String PREFIX = "src/test/resources/io/";

    /**
     * Test present file.
     */
    @Test
    public void testPresentFile() {
        File file = createPresentTestFile("file");
        assertTrue(file.isFile());
        assertFalse(file.isDirectory());
    }

    /**
     * Test present archive.
     */
    @Test
    public void testPresentArchive() {
        File file = createPresentTestFile("archive.zip");
        assertTrue(file.isFile());
        assertTrue(file.isDirectory());
    }

    /**
     * Test present directory.
     */
    @Test
    public void testPresentDirectory() {
        File file = createPresentTestDirectory("directory");
        assertFalse(file.isFile());
        assertTrue(file.isDirectory());
    }

    /**
     * Test absent file.
     */
    @Test
    public void testAbsentFile() {
        File file = createAbsentTestFile("file");
        assertFalse(file.isFile());
        assertFalse(file.isDirectory());
    }

    /**
     * Test absent archive.
     */
    @Test
    public void testAbsentArchive() {
        File file = createAbsentTestFile("archive.zip");
        assertFalse(file.isFile());
        assertFalse(file.isDirectory());
    }

    /**
     * Test absent directory.
     */
    @Test
    public void testAbsentDirectory() {
        File file = createAbsentTestDirectory("directory");
        assertFalse(file.isFile());
        assertFalse(file.isDirectory());
    }

    /**
     * Normal file.
     */
    @Test
    public void testFile001() throws Exception {
        File file = I.locate(PREFIX + "test001/1.txt");
        assertFile(file, "1.txt", "1");
    }

    /**
     * Normal file. Test {@resolve File#getParentFile()}.
     */
    @Test
    public void testFile002() throws Exception {
        File file = I.locate(PREFIX + "test001/1.txt");
        File parent = file.getParentFile();

        assertDirectory(parent, "test001");
    }

    /**
     * Normal file. Test {@resolve File#getParent()}.
     */
    @Test
    public void testFile003() throws Exception {
        File file = I.locate(PREFIX + "test001/1.txt");
        String parentPath = file.getParent();

        File parent = I.locate(parentPath);
        assertDirectory(parent, "test001");
    }

    /**
     * Normal file. Test {@resolve File#list()}.
     */
    @Test
    public void testFile004() throws Exception {
        File file = I.locate(PREFIX + "test001/1.txt");
        String[] list = file.list();

        assertNull(list);
    }

    /**
     * Normal file. Test {@resolve File#listFiles()}.
     */
    @Test
    public void testFile005() throws Exception {
        File file = I.locate(PREFIX + "test001/1.txt");
        File[] list = file.listFiles();

        assertNull(list);
    }

    /**
     * Create and delete normal file.
     */
    @Test
    public void testFile006() throws Exception {
        File file = I.locate(PREFIX + "test001/create.txt");

        assertNotNull(file);
        assertEquals("create.txt", file.getName());
        assertFalse(file.exists());

        // create
        assertTrue(file.createNewFile());
        assertTrue(file.exists());

        // delete
        assertTrue(file.delete());
        assertFalse(file.exists());
    }

    /**
     * Normal directory
     */
    @Test
    public void testFile050() throws Exception {
        File file = I.locate(PREFIX + "test002/test");
        assertDirectory(file, "test");
    }

    /**
     * Normal directory. Test {@resolve File#getParentFile()}.
     */
    @Test
    public void testFile051() throws Exception {
        File file = I.locate(PREFIX + "test002/test");
        File parent = file.getParentFile();

        assertDirectory(parent, "test002");
    }

    /**
     * Normal directory. Test {@resolve File#getParent()}.
     */
    @Test
    public void testFile052() throws Exception {
        File file = I.locate(PREFIX + "test002/test");
        String parentPath = file.getParent();

        File parent = I.locate(parentPath);
        assertDirectory(parent, "test002");
    }

    /**
     * Normal directory. Test {@resolve File#list()}.
     */
    @Test
    public void testFile053() throws Exception {
        File file = I.locate(PREFIX + "test002/test");
        String[] list = file.list();

        assertChildren(list, 3, "a.txt", "b.txt", "c.txt");
    }

    /**
     * Normal directory. Test {@resolve File#listFiles()}.
     */
    @Test
    public void testFile054() throws Exception {
        File file = I.locate(PREFIX + "test002/test");
        File[] list = file.listFiles();

        assertChildren(list, 3, "a.txt", "b.txt", "c.txt");
    }

    /**
     * Make and delete normal directory which has archive like name.
     */
    @Test
    public void testFile055() throws Exception {
        File file = createAbsentTestDirectory("directoryLikeArchiveName.zip");
        assertNotNull(file);
        assertEquals("directoryLikeArchiveName.zip", file.getName());
        assertFalse(file.exists());

        // create
        assertTrue(file.mkdir());
        assertTrue(file.exists());
        assertTrue(file.isDirectory());
        assertFalse(file.isFile());
    }

    /**
     * Make and delete normal directory.
     */
    @Test
    public void testFile056() throws Exception {
        File file = I.locate(PREFIX + "test002/make");

        assertNotNull(file);
        assertEquals("make", file.getName());
        assertFalse(file.exists());

        // create
        assertTrue(file.mkdir());
        assertTrue(file.exists());

        // delete
        assertTrue(file.delete());
        assertFalse(file.exists());
    }

    /**
     * Make and delete normal directory.
     */
    @Test
    public void testFile057() throws Exception {
        File file = I.locate(PREFIX + "test002/make/dir/dir");

        assertNotNull(file);
        assertEquals("dir", file.getName());
        assertFalse(file.exists());

        // create
        assertTrue(file.mkdirs());
        assertTrue(file.exists());

        // delete
        File make = I.locate(PREFIX + "test002/make");
        assertTrue(FileSystem.delete(make));
        assertFalse(file.exists());
        assertFalse(make.exists());
    }

    /**
     * File in Zip.
     */
    @Test
    public void testFile100() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/1.txt");
        assertFile(file, "1.txt", "1");
    }

    /**
     * File in Zip. Test {@resolve File#getParentFile()}.
     */
    @Test
    public void testFile101() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/1.txt");
        File parent = file.getParentFile();
        assertArchive(parent, "test.zip");

        parent = parent.getParentFile();

        assertDirectory(parent, "test003");
    }

    /**
     * File in Zip. Test {@resolve File#getParent()}.
     */
    @Test
    public void testFile102() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/1.txt");

        String parentPath = file.getParent();
        File parent = I.locate(parentPath);

        assertArchive(parent, "test.zip");

        parentPath = parent.getParent();
        parent = I.locate(parentPath);

        assertDirectory(parent, "test003");
    }

    /**
     * File in Zip.Test {@resolve File#list()}.
     */
    @Test
    public void testFile103() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/1.txt");
        String[] list = file.list();

        assertNull(list);
    }

    /**
     * File in Zip.Test {@resolve File#listFiles()}.
     */
    @Test
    public void testFile104() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/1.txt");
        File[] list = file.listFiles();

        assertNull(list);
    }

    /**
     * File in Zip. Writing Test.
     */
    @Test(expected = FileNotFoundException.class)
    public void testFile105() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/1.txt");
        assertFile(file, "1.txt", "1");

        FileWriter writer = null;

        try {
            writer = new FileWriter(file);
            writer.append("write");
        } finally {
            FileSystem.close(writer);
        }
    }

    /**
     * File in Zip. Delete Test.
     */
    @Test
    public void testFile106() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/1.txt");
        assertFile(file, "1.txt", "1");
        assertFalse(file.delete());
    }

    /**
     * File in Zip. Access Permission
     */
    @Test
    public void testFile107() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/1.txt");
        assertFile(file, "1.txt", "1");

        assertTrue(file.canRead());
        assertFalse(file.canWrite());
    }

    /**
     * Nonexistence file in Zip.
     */
    @Test
    public void testFile108() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/nonexistence.txt");

        assertNotNull(file);
        assertEquals("nonexistence.txt", file.getName());
        assertFalse(file.exists());
    }

    /**
     * Create file in Zip.
     */
    @Test
    public void testFile109() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/create.txt");

        assertNotNull(file);
        assertEquals("create.txt", file.getName());
        assertFalse(file.exists());
        assertFalse(file.createNewFile());
        assertFalse(file.exists());
    }

    /**
     * Directory in Zip.
     */
    @Test
    public void testFile150() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/test");
        assertDirectory(file, "test");
    }

    /**
     * Directory in Zip. Test {@resolve File#getParentFile()}.
     */
    @Test
    public void testFile151() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/test");
        File parent = file.getParentFile();

        assertArchive(parent, "test.zip");

        parent = parent.getParentFile();

        assertDirectory(parent, "test003");
    }

    /**
     * Directory in Zip. Test {@resolve File#getParent()}.
     */
    @Test
    public void testFile152() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/test");

        String parentPath = file.getParent();
        File parent = I.locate(parentPath);

        assertArchive(parent, "test.zip");

        parentPath = parent.getParent();
        parent = I.locate(parentPath);

        assertDirectory(parent, "test003");
    }

    /**
     * Directory in Zip.Test {@resolve File#list()}.
     */
    @Test
    public void testFile153() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/test");
        String[] list = file.list();

        assertChildren(list, 3, "a.txt", "b.txt", "c.txt");
    }

    /**
     * Directory in Zip.Test {@resolve File#listFiles()}.
     */
    @Test
    public void testFile154() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/test");
        File[] list = file.listFiles();

        assertChildren(list, 3, "a.txt", "b.txt", "c.txt");
    }

    /**
     * Nonexistence directory in Zip.
     */
    @Test
    public void testFile155() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/nonexistence");

        assertNotNull(file);
        assertEquals("nonexistence", file.getName());
        assertFalse(file.exists());
    }

    /**
     * Make directory in Zip.
     */
    @Test
    public void testFile156() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/make");

        assertNotNull(file);
        assertEquals("make", file.getName());
        assertFalse(file.exists());
        assertFalse(file.mkdir());
        assertFalse(file.exists());
    }

    /**
     * Create directory in Zip.
     */
    @Test
    public void testFile157() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/create");

        assertNotNull(file);
        assertEquals("create", file.getName());
        assertFalse(file.exists());
        assertFalse(file.createNewFile());
        assertFalse(file.exists());
    }

    /**
     * Direct Zip.
     */
    @Test
    public void testFile200() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip");
        assertArchive(file, "test.zip");
    }

    /**
     * Directory in Zip. Test {@resolve File#getParentFile()}.
     */
    @Test
    public void testFile201() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip");
        File parent = file.getParentFile();

        assertDirectory(parent, "test003");

        parent = parent.getParentFile();

        assertDirectory(parent, "io");
    }

    /**
     * Directory in Zip. Test {@resolve File#getParent()}.
     */
    @Test
    public void testFile202() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip");

        String parentPath = file.getParent();
        File parent = I.locate(parentPath);

        assertDirectory(parent, "test003");

        parentPath = parent.getParent();
        parent = I.locate(parentPath);

        assertDirectory(parent, "io");
    }

    /**
     * Directory in Zip.Test {@resolve File#list()}.
     */
    @Test
    public void testFile203() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip");
        String[] list = file.list();

        assertChildren(list, 3, "test", "1.txt", "2.txt");
    }

    /**
     * Directory in Zip.Test {@resolve File#listFiles()}.
     */
    @Test
    public void testFile204() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip");
        File[] list = file.listFiles();

        assertChildren(list, 3, "test", "1.txt", "2.txt");
    }

    /**
     * Lastmodified time of Zip.
     */
    @Test
    public void testFile205() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip");
        assertArchive(file, "test.zip");
        assertTrue(file instanceof ezbean.io.File);

        ezbean.io.File archive = (ezbean.io.File) file;
        assertFalse(archive.getJunction().exists());

        // enforce this archive unpacked
        archive.list();

        assertTrue(archive.getJunction().exists());
        assertEquals(file.lastModified(), archive.getJunction().lastModified());
    }

    /**
     * Lastmodified time of file in Zip.
     */
    @Test
    public void testFile206() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/1.txt");
        assertFile(file, "1.txt");

        File zipFile = I.locate(PREFIX + "test003/test.zip");
        ZipEntry entry = new java.util.zip.ZipFile(zipFile).getEntry("1.txt");
        assertNotNull(entry);
        assertEquals(entry.getTime(), file.lastModified());
    }

    /**
     * Zip from Normal FS with no compression.
     */
    @Test
    public void testFile230() throws Exception {
        File file = I.locate(PREFIX + "test003");
        assertDirectory(file, "test003");

        File[] files = file.listFiles();
        assertChildren(files, 2, "compressed.zip", "test.zip");

        File zip = files[1];
        assertArchive(zip, "test.zip");

        files = zip.listFiles();
        assertChildren(files, 3, "test", "1.txt", "2.txt");

        File txt1 = getFile(files, "1.txt");
        assertFile(txt1, "1.txt", "1");

        File txt2 = getFile(files, "2.txt");
        assertFile(txt2, "2.txt", "2");
    }

    /**
     * Zip from Normal FS with compression.
     */
    @Test
    public void testFile231() throws Exception {
        File file = I.locate(PREFIX + "test003");
        assertDirectory(file, "test003");

        File[] files = file.listFiles();
        assertChildren(files, 2, "compressed.zip", "test.zip");

        File zip = files[0];
        assertArchive(zip, "compressed.zip");

        files = zip.listFiles();
        assertChildren(files, 3, "test", "1.txt", "2.txt");

        File txt1 = getFile(files, "1.txt");
        assertFile(txt1, "1.txt", "11111111111111111111");

        File txt2 = getFile(files, "2.txt");
        assertFile(txt2, "2.txt", "22222222222222222222");
    }

    /**
     * Nested Zip.
     */
    @Test
    public void testFile251() throws Exception {
        File file = I.locate(PREFIX + "test004/test.zip/nest.zip/test/a.txt");
        assertFile(file, "a.txt", "a");

        File parent = file.getParentFile();
        assertDirectory(parent, "test");

        parent = parent.getParentFile();
        assertArchive(parent, "nest.zip");

        parent = parent.getParentFile();
        assertArchive(parent, "test.zip");

        parent = parent.getParentFile();
        assertDirectory(parent, "test004");
    }

    /**
     * Nested Zip.
     */
    @Test
    public void testFile252() throws Exception {
        File file = I.locate(PREFIX + "test005/test.zip/nest1/nest1.zip/nest2/nest2.zip/test/b.txt");
        assertFile(file, "b.txt", "b");

        File parent = file.getParentFile();
        assertDirectory(parent, "test");

        parent = parent.getParentFile();
        assertArchive(parent, "nest2.zip");

        parent = parent.getParentFile();
        assertDirectory(parent, "nest2");

        parent = parent.getParentFile();
        assertArchive(parent, "nest1.zip");

        parent = parent.getParentFile();
        assertDirectory(parent, "nest1");

        parent = parent.getParentFile();
        assertArchive(parent, "test.zip");

        parent = parent.getParentFile();
        assertDirectory(parent, "test005");
    }

    /**
     * Non ASCII in Zip.
     */
    @Test
    public void testFile253() throws Exception {
        File file = I.locate(PREFIX + "test006/nonASCII.zip");
        assertArchive(file, "nonASCII.zip");
    }

    /**
     * Non ASCII in Zip.
     */
    @Test
    public void testFile254() throws Exception {
        File file = I.locate(PREFIX + "test006/nonASCII.zip/あ.txt");
        assertFile(file, "あ.txt", "あ");
    }

    /**
     * Test toString of zip.
     */
    @Test
    public void testToString01() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip");
        assertArchive(file, "test.zip");

        assertEquals(PREFIX + "test003/test.zip", file.toString());
    }

    /**
     * Test toString of file in zip.
     */
    @Test
    public void testToString02() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip/1.txt");
        assertFile(file, "1.txt");

        assertEquals(PREFIX + "test003/test.zip/1.txt", file.toString());
    }

    /**
     * Test toURL of zip.
     */
    @Test
    public void testToURL1() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip");
        assertArchive(file, "test.zip");

        File expected = new File(PREFIX + "test003/test.zip");
        assertEquals(expected.toURI().toURL(), file.toURI().toURL());
    }

    /**
     * Test toURI of zip.
     */
    @Test
    public void testToURI1() throws Exception {
        File file = I.locate(PREFIX + "test003/test.zip");
        assertArchive(file, "test.zip");

        File expected = new File(PREFIX + "test003/test.zip");
        assertEquals(expected.toURI(), file.toURI());
    }

    /**
     * Test getParentFile.
     */
    @Test
    public void testParentFile1() throws Exception {
        File file = new File("test");
        assertNotNull(file);
        assertNull(file.getParentFile());

        file = I.locate("test");
        assertNotNull(file);
        assertNull(file.getParentFile());
    }
}
