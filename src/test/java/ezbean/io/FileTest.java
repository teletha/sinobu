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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Rule;
import org.junit.Test;

import ezbean.I;
import ezunit.CleanRoom;

/**
 * @version 2010/02/13 14:52:46
 */
public class FileTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    /**
     * Normal file.
     */
    @Test
    public void file() throws Exception {
        File file = room.locateFile("directory/1");
        assertFile(file, "1");
    }

    /**
     * Normal file. Test {@resolve File#getParentFile()}.
     */
    @Test
    public void fileCanGetParentFile() throws Exception {
        File file = room.locateFile("directory/1");
        File parent = file.getParentFile();

        assertDirectory(parent);
    }

    /**
     * Normal file. Test {@resolve File#getParent()}.
     */
    @Test
    public void fileCanGetParentPath() throws Exception {
        File file = room.locateFile("directory/1");
        String parentPath = file.getParent();

        File parent = I.locate(parentPath);
        assertDirectory(parent);
    }

    /**
     * Normal file. Test {@resolve File#list()}.
     */
    @Test
    public void fileCantListupChildren() throws Exception {
        File file = room.locateFile("directory/1");

        assertNull(file.list());
    }

    /**
     * Normal file. Test {@resolve File#listFiles()}.
     */
    @Test
    public void fileCantListupChildrenFiles() throws Exception {
        File file = room.locateFile("directory/1");
        File[] list = file.listFiles();

        assertNull(list);
    }

    /**
     * Create and delete normal file.
     */
    @Test
    public void createAndDeleteFile() throws Exception {
        File file = room.locateAbsent("directory/create");

        assertNotNull(file);
        assertEquals("create", file.getName());
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
    public void directory() throws Exception {
        File file = room.locateDirectory("directory/child");
        assertDirectory(file);
    }

    /**
     * Normal directory. Test {@resolve File#getParentFile()}.
     */
    @Test
    public void directoryCanGetParentFile() throws Exception {
        File file = room.locateDirectory("directory/child");
        File parent = file.getParentFile();

        assertDirectory(parent);
    }

    /**
     * Normal directory. Test {@resolve File#getParent()}.
     */
    @Test
    public void directoryCanGetParentPath() throws Exception {
        File file = room.locateDirectory("directory/child");
        String parentPath = file.getParent();

        File parent = I.locate(parentPath);
        assertDirectory(parent);
    }

    /**
     * Normal directory. Test {@resolve File#list()}.
     */
    @Test
    public void directoryCanListupChildrenPaths() throws Exception {
        File file = room.locateDirectory("directory/child");
        String[] list = file.list();

        assertChildren(list, 3, "a", "b", "c");
    }

    /**
     * Normal directory. Test {@resolve File#listFiles()}.
     */
    @Test
    public void directoryCanListupChildrenFiles() throws Exception {
        File file = room.locateDirectory("directory/child");
        File[] list = file.listFiles();

        assertChildren(list, 3, "a", "b", "c");
    }

    /**
     * Make and delete normal directory which has archive like name.
     */
    @Test
    public void createAndDeleteDirectoryWhichNameIsLikeArchive() throws Exception {
        File file = room.locateAbsent("directoryLikeArchiveName.zip");

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
    public void createAndDeleteDirectory() throws Exception {
        File file = room.locateAbsent("directory/make");

        assertNotNull(file);
        assertEquals("make", file.getName());
        assertFalse(file.exists());
        assertTrue(file.getParentFile().exists());

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
    public void createAndDeleteNestedDirectory() throws Exception {
        File file = room.locateAbsent("directory/make/dir/dir");

        assertFalse(file.exists());
        assertFalse(file.getParentFile().exists());
        assertFalse(file.getParentFile().getParentFile().exists());
        assertTrue(file.getParentFile().getParentFile().getParentFile().exists());

        // create
        assertTrue(file.mkdirs());
        assertTrue(file.exists());

        // delete
        File make = room.locateDirectory("directory/make");
        assertTrue(FileSystem.delete(make));
        assertFalse(file.exists());
        assertFalse(make.exists());
    }

    /**
     * File in Zip.
     */
    @Test
    public void fileInArchive() throws Exception {
        File file = room.locateFile("archive/test.zip/1.txt");
        assertFile(file, "1");
    }

    /**
     * File in Zip. Test {@resolve File#getParentFile()}.
     */
    @Test
    public void fileInArchiveCanGetParentFile() throws Exception {
        File file = room.locateFile("archive/test.zip/1.txt");
        File parent = file.getParentFile();
        assertArchive(parent);

        parent = parent.getParentFile();

        assertDirectory(parent);
    }

    /**
     * File in Zip. Test {@resolve File#getParent()}.
     */
    @Test
    public void fileInArchiveCanGetParentPath() throws Exception {
        File file = room.locateFile("archive/test.zip/1.txt");

        String parentPath = file.getParent();
        File parent = I.locate(parentPath);

        assertArchive(parent);

        parentPath = parent.getParent();
        parent = I.locate(parentPath);

        assertDirectory(parent);
    }

    /**
     * File in Zip.Test {@resolve File#list()}.
     */
    @Test
    public void fileInArchiveCantListupChildrePaths() throws Exception {
        File file = room.locateFile("archive/test.zip/1.txt");
        String[] list = file.list();

        assertNull(list);
    }

    /**
     * File in Zip.Test {@resolve File#listFiles()}.
     */
    @Test
    public void fileInArchiveCantListupChildreFiles() throws Exception {
        File file = room.locateFile("archive/test.zip/1.txt");
        File[] list = file.listFiles();

        assertNull(list);
    }

    /**
     * File in Zip. Writing Test.
     */
    @Test(expected = FileNotFoundException.class)
    public void fileInArchiveIsNotWritable() throws Exception {
        File file = room.locateFile("archive/test.zip/1.txt");
        assertFile(file, "1");

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
    public void fileInArchiveCantDelete() throws Exception {
        File file = room.locateFile("archive/test.zip/1.txt");
        assertFile(file, "1");
        assertFalse(file.delete());
    }

    /**
     * File in Zip. Access Permission
     */
    @Test
    public void fileInArchiveIsReadableAndUnwritable() throws Exception {
        File file = room.locateFile("archive/test.zip/1.txt");
        assertFile(file, "1");

        assertTrue(file.canRead());
        assertFalse(file.canWrite());
    }

    /**
     * Nonexistence file in Zip.
     */
    @Test
    public void absentFileInArchive() throws Exception {
        File file = room.locateAbsent("archive/test.zip/nonexistence.txt");

        assertNotNull(file);
        assertEquals("nonexistence.txt", file.getName());
        assertFalse(file.exists());
    }

    /**
     * Create file in Zip.
     */
    @Test
    public void fileInArchiveCantCreate() throws Exception {
        File file = room.locateAbsent("archive/test.zip/create.txt");

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
    public void directoryInArchive() throws Exception {
        File file = room.locateDirectory("archive/test.zip/test");
        assertDirectory(file);
    }

    /**
     * Directory in Zip. Test {@resolve File#getParentFile()}.
     */
    @Test
    public void directoryInArchiveCanGetParentFile() throws Exception {
        File file = room.locateDirectory("archive/test.zip/test");
        File parent = file.getParentFile();

        assertArchive(parent);

        parent = parent.getParentFile();

        assertDirectory(parent);
    }

    /**
     * Directory in Zip. Test {@resolve File#getParent()}.
     */
    @Test
    public void directoryInArchiveCanGetParentPath() throws Exception {
        File file = room.locateDirectory("archive/test.zip/test");

        String parentPath = file.getParent();
        File parent = I.locate(parentPath);

        assertArchive(parent);

        parentPath = parent.getParent();
        parent = I.locate(parentPath);

        assertDirectory(parent);
    }

    /**
     * Directory in Zip.Test {@resolve File#list()}.
     */
    @Test
    public void directoryInArchiveCanListupChildrePaths() throws Exception {
        File file = room.locateDirectory("archive/test.zip/test");
        String[] list = file.list();

        assertChildren(list, 3, "a.txt", "b.txt", "c.txt");
    }

    /**
     * Directory in Zip.Test {@resolve File#listFiles()}.
     */
    @Test
    public void directoryInArchiveCanListupChildreFiles() throws Exception {
        File file = room.locateDirectory("archive/test.zip/test");
        File[] list = file.listFiles();

        assertChildren(list, 3, "a.txt", "b.txt", "c.txt");
    }

    /**
     * Nonexistence directory in Zip.
     */
    @Test
    public void absentDirectoryInArchive() throws Exception {
        File file = room.locateAbsent("archive/test.zip/nonexistence");

        assertNotNull(file);
        assertEquals("nonexistence", file.getName());
        assertFalse(file.exists());
    }

    /**
     * Make directory in Zip.
     */
    @Test
    public void directoryInArchiveCantMake() throws Exception {
        File file = room.locateAbsent("archive/test.zip/make");

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
    public void directoryInArchiveCantCreate() throws Exception {
        File file = room.locateAbsent("archive/test.zip/create");

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
    public void archive() throws Exception {
        File file = room.locateFile("archive/test.zip");
        assertArchive(file);
    }

    /**
     * Directory in Zip. Test {@resolve File#getParentFile()}.
     */
    @Test
    public void archiveCanGetParentFile() throws Exception {
        File file = room.locateFile("archive/test.zip");
        File parent = file.getParentFile();

        assertDirectory(parent);
    }

    /**
     * Directory in Zip. Test {@resolve File#getParent()}.
     */
    @Test
    public void archiveCanGetParentPath() throws Exception {
        File file = room.locateFile("archive/test.zip");

        String parentPath = file.getParent();
        File parent = I.locate(parentPath);

        assertDirectory(parent);
    }

    /**
     * Directory in Zip.Test {@resolve File#list()}.
     */
    @Test
    public void archiveCanListupChildrenPaths() throws Exception {
        File file = room.locateFile("archive/test.zip");
        String[] list = file.list();

        assertChildren(list, 3, "test", "1.txt", "2.txt");
    }

    /**
     * Directory in Zip.Test {@resolve File#listFiles()}.
     */
    @Test
    public void archiveCanListupChildrenFiles() throws Exception {
        File file = room.locateFile("archive/test.zip");
        File[] list = file.listFiles();

        assertChildren(list, 3, "test", "1.txt", "2.txt");
    }

    /**
     * Lastmodified time of Zip.
     */
    @Test
    public void archiveCanRestoreItsLastModifiedData() throws Exception {
        File file = room.locateFile("archive/test.zip");
        assertArchive(file);
        assertTrue(file instanceof ezbean.io.File);

        ezbean.io.File archive = (ezbean.io.File) file;
        assertEquals(file.lastModified(), archive.getJunction().lastModified());
    }

    /**
     * Lastmodified time of file in Zip.
     */
    @Test
    public void fileInArchiveCanRestoreItsLastModifiedData() throws Exception {
        File file = room.locateFile("archive/test.zip/1.txt");
        assertFile(file);

        ZipFile zip = null;

        try {
            zip = new ZipFile(room.locateFile("archive/test.zip"));
            ZipEntry entry = zip.getEntry("1.txt");
            assertNotNull(entry);
            assertEquals(entry.getTime(), file.lastModified());
        } finally {
            if (zip != null) {
                zip.close();
            }
        }
    }

    /**
     * Zip from Normal FS with no compression.
     */
    @Test
    public void archiveWithoutCompression() throws Exception {
        File file = room.locateDirectory("archive");
        assertDirectory(file);

        File[] files = file.listFiles();

        File zip = getFile(files, "test.zip");
        assertArchive(zip);

        files = zip.listFiles();
        assertChildren(files, 3, "test", "1.txt", "2.txt");

        File txt1 = getFile(files, "1.txt");
        assertFile(txt1, "1");

        File txt2 = getFile(files, "2.txt");
        assertFile(txt2, "2");
    }

    /**
     * Zip from Normal FS with compression.
     */
    @Test
    public void archiveWithCompression() throws Exception {
        File file = room.locateDirectory("archive");
        assertDirectory(file);

        File[] files = file.listFiles();
        File zip = getFile(files, "compressed.zip");
        assertArchive(zip);

        files = zip.listFiles();
        assertChildren(files, 3, "test", "1.txt", "2.txt");

        File txt1 = getFile(files, "1.txt");
        assertFile(txt1, "11111111111111111111");

        File txt2 = getFile(files, "2.txt");
        assertFile(txt2, "22222222222222222222");
    }

    /**
     * Nested Zip.
     */
    @Test
    public void nestedArchive() throws Exception {
        File file = room.locateFile("archive/nest.zip/nest.zip/test/a.txt");
        assertFile(file, "a");

        File parent = file.getParentFile();
        assertDirectory(parent);

        parent = parent.getParentFile();
        assertArchive(parent);

        parent = parent.getParentFile();
        assertArchive(parent);

        parent = parent.getParentFile();
        assertDirectory(parent);
    }

    /**
     * Nested Zip.
     */
    @Test
    public void moreNestedArchive() throws Exception {
        File file = room.locateFile("archive/deepNest.zip/nest1/nest1.zip/nest2/nest2.zip/test/b.txt");
        assertFile(file, "b");

        File parent = file.getParentFile();
        assertDirectory(parent);

        parent = parent.getParentFile();
        assertArchive(parent);

        parent = parent.getParentFile();
        assertDirectory(parent);

        parent = parent.getParentFile();
        assertArchive(parent);

        parent = parent.getParentFile();
        assertDirectory(parent);

        parent = parent.getParentFile();
        assertArchive(parent);

        parent = parent.getParentFile();
        assertDirectory(parent);
    }

    /**
     * Non ASCII in Zip.
     */
    @Test
    public void nonASCIIArchive() throws Exception {
        File file = room.locateFile("archive/nonASCII.zip");
        assertArchive(file);
    }

    /**
     * Non ASCII in Zip.
     */
    @Test
    public void fileInNonASCIIArchive() throws Exception {
        room.assume("MS932");

        File file = room.locateFile("archive/nonASCII.zip/あ.txt");
        assertFile(file, "あ");
    }

    private static final String PREFIX = "src/test/resources/ezbean/io/";

    /**
     * Test toString of zip.
     */
    @Test
    public void archiveFileOutputsUserFriendlyPath() throws Exception {
        File file = I.locate(PREFIX + "archive/test.zip");
        assertArchive(file);

        assertEquals(PREFIX + "archive/test.zip", file.toString());
    }

    /**
     * Test toString of file in zip.
     */
    @Test
    public void fileInArchiveFileOutputsUserFriendlyPath() throws Exception {
        File file = I.locate(PREFIX + "archive/test.zip/1.txt");
        assertFile(file);

        assertEquals(PREFIX + "archive/test.zip/1.txt", file.toString());
    }

    /**
     * Test toURL of zip.
     */
    @Test
    public void archiveFileOutputsUserFriendlyURL() throws Exception {
        File file = I.locate(PREFIX + "archive/test.zip");
        assertArchive(file);

        File expected = new File(PREFIX + "archive/test.zip");
        assertEquals(expected.toURI().toURL(), file.toURI().toURL());
    }

    /**
     * Test toURI of zip.
     */
    @Test
    public void fileInArchiveFileOutputsUserFriendlyURL() throws Exception {
        File file = I.locate(PREFIX + "archive/test.zip");
        assertArchive(file);

        File expected = new File(PREFIX + "archive/test.zip");
        assertEquals(expected.toURI(), file.toURI());
    }

    /**
     * Test getParentFile.
     */
    @Test
    public void topLebelFileCantHaveParentFile() throws Exception {
        File file = new File("test");
        assertNotNull(file);
        assertNull(file.getParentFile());

        file = I.locate("test");
        assertNotNull(file);
        assertNull(file.getParentFile());
    }

    /**
     * Helper method to retrieve named file.
     * 
     * @param files
     * @param name
     * @return
     */
    private static File getFile(File[] files, String name) {
        for (File file : files) {
            if (name.equals(file.getName())) {
                return file;
            }
        }
        return null;
    }
}
