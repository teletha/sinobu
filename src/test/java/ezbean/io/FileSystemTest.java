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

import static org.junit.Assert.*;


import java.io.File;



import org.junit.Test;

import ezbean.I;
import ezbean.io.FileSystem;

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: FileSystemTest.java,v 1.0 2007/03/03 0:55:40 Teletha Exp $
 */
public class FileSystemTest extends FileSystemTestCase {

    /**
     * Test method for {@link ezbean.io.FileSystem#link(java.lang.String)}.
     */
    @Test(expected = NullPointerException.class)
    public void testLocate01() {
        I.locate(null);
    }

    /**
     * Test method for {@resolve ezbean.io.FileSystem#locate(java.lang.String)}.
     */
    @Test
    public void testLocate02() {
        File file = I.locate("");
        File expected = new File("");

        assertFilePathEquals(expected, file);
    }

    /**
     * Test method for {@resolve ezbean.io.FileSystem#locate(java.lang.String)}.
     */
    @Test
    public void testLocate03() {
        File file = I.locate("foo");
        File expected = new File("foo");

        assertFilePathEquals(expected, file);
    }

    /**
     * Test method for {@resolve ezbean.io.FileSystem#locate(java.lang.String)}.
     */
    @Test
    public void testLocate04() {
        File file = I.locate("foo/bar");
        File expected = new File("foo/bar");

        assertFilePathEquals(expected, file);
    }

    /**
     * Test method for {@resolve ezbean.io.FileSystem#locate(java.lang.String)}.
     */
    @Test
    public void testLocate05() {
        File file = I.locate("foo/bar.txt");
        File expected = new File("foo/bar.txt");

        assertFilePathEquals(expected, file);
    }

    /**
     * Test method for {@resolve ezbean.io.FileSystem#locate(java.lang.String)}.
     */
    @Test
    public void testLocate06() {
        File file = I.locate("foo.zip");
        File expected = new File("foo.zip");

        assertFilePathEquals(expected, file);
    }

    /**
     * Test method for {@resolve ezbean.io.FileSystem#locate(java.lang.String)}.
     */
    @Test
    public void testLocate07() {
        File file = I.locate("foo/bar.zip");
        File expected = new File("foo/bar.zip");

        assertFilePathEquals(expected, file);
    }

    /**
     * Test method for {@resolve ezbean.io.FileSystem#locate(java.lang.String)}.
     */
    @Test
    public void testLocate08() {
        String absolutePath = new File("").getAbsolutePath() + "/";

        File file = I.locate(absolutePath + "foo/bar.zip");
        File expected = new File(absolutePath + "foo/bar.zip");

        assertFilePathEquals(expected, file);
    }

    /**
     * Test method for {@resolve ezbean.io.FileSystem#locate(java.lang.String)}.
     */
    @Test
    public void testLocate09() {
        String absolutePath = FileSystem.temporaries.getAbsolutePath() + FileSystem.SEPARATOR + "test.txt";

        File file = I.locate(absolutePath);
        File expected = new File(absolutePath);

        assertFilePathEquals(expected, file);
        assertEquals(expected.toString(), file.toString().replace(FileSystem.SEPARATOR, File.separatorChar));
    }

    /**
     * Test method for {@resolve ezbean.io.FileSystem#locate(java.lang.String)}.
     */
    @Test
    public void testLocate10() {
        File file = I.locate("root/../file");
        File expected = new File("root/../file");

        assertFilePathEquals(expected, file);
    }

    private static final String PREFIX = new File("").toURI().toASCIIString() + "/src/test/resources/io/";

    /**
     * File protocol.
     */
    @Test
    public void testProtocol01() {
        File file = I.locate(PREFIX + "test001/1.txt");
        assertFile(file, "1.txt");
    }

    /**
     * File protocol.
     */
    @Test
    public void testProtocol02() {
        File file = I.locate(PREFIX + "test002/test");
        assertDirectory(file, "test");
    }

    /**
     * File protocol.
     */
    @Test
    public void testProtocol03() {
        File file = I.locate(PREFIX + "test003/test.zip/1.txt");
        assertFile(file, "1.txt");
    }

    /**
     * File protocol.
     */
    @Test
    public void testProtocol04() {
        File file = I.locate(PREFIX + "test003/test.zip/test");
        assertDirectory(file, "test");
    }
}
