/**
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
package ezbean.io;

import static ezunit.Ezunit.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import ezbean.I;

/**
 * @version 2010/02/10 19:44:08
 */
public class FileSystemTest {

    @Test(expected = NullPointerException.class)
    public void locateNull() {
        I.locate((String) null);
    }

    @Test
    public void locateEmpty() {
        File file = I.locate("");
        File expected = new File("");

        assertFileEquals(expected, file);
    }

    @Test
    public void locate() {
        File file = I.locate("foo");
        File expected = new File("foo");

        assertFileEquals(expected, file);
    }

    @Test
    public void locateSlash() {
        File file = I.locate("foo/bar");
        File expected = new File("foo/bar");

        assertFileEquals(expected, file);
    }

    @Test
    public void locateFile() {
        File file = I.locate("foo/bar.txt");
        File expected = new File("foo/bar.txt");

        assertFileEquals(expected, file);
    }

    @Test
    public void locateArchive() {
        File file = I.locate("foo.zip");
        File expected = new File("foo.zip");

        assertFileEquals(expected, file);
    }

    @Test
    public void locateArchiveInDirectory() {
        File file = I.locate("foo/bar.zip");
        File expected = new File("foo/bar.zip");

        assertFileEquals(expected, file);
    }

    @Test
    public void locateAbsoluteArchive() {
        String absolutePath = new File("").getAbsolutePath() + "/";

        File file = I.locate(absolutePath + "foo/bar.zip");
        File expected = new File(absolutePath + "foo/bar.zip");

        assertFileEquals(expected, file);
    }

    @Test
    public void locateTemporary() {
        String absolutePath = System.getProperty("java.io.tmpdir") + "test";

        File file = I.locate(absolutePath);
        File expected = new File(absolutePath);

        assertFileEquals(expected, file);
        assertEquals(expected.toString(), file.toString().replace('/', File.separatorChar));
    }

    @Test
    public void locateRelative() {
        File file = I.locate("root/../file");
        File expected = new File("root/../file");

        assertFileEquals(expected, file);
    }

    /** file protocol. */
    private static final String FILE_PROTOCOL ="file://" + locatePackage(FileSystemTest.class).getAbsolutePath().replace(File.separatorChar, '/') + "/";

    @Test
    public void withProtocol1() {
        File file = I.locate(FILE_PROTOCOL + "directory/1");
        assertFile(file);
    }

    @Test
    public void withProtocol2() {
        File file = I.locate(FILE_PROTOCOL + "directory/child");
        assertDirectory(file);
    }

    @Test
    public void withProtocol3() {
        File file = I.locate(FILE_PROTOCOL + "archive/test.zip/1.txt");
        assertFile(file);
    }

    @Test
    public void withProtocol4() {
        File file = I.locate(FILE_PROTOCOL + "archive/test.zip/test");
        assertDirectory(file);
    }
}
