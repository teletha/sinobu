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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import ezbean.I;

/**
 * DOCUMENT.
 * 
 * @version 2008/12/01 19:12:33
 */
public abstract class FileSystemTestCase {

    /** The isolation directory to test. */
    private static File isolation = new File("target/isolation");

    /** The original location of temporary. */
    private static File original;

    /**
     * Delete isolation directory.
     */
    @BeforeClass
    public static void beforeClass() {
        isolation.mkdirs();

        // store original location
        original = FileSystem.temporaries;

        // move a temporary directory to the isolation directory for test
        FileSystem.temporaries = isolation;
    }

    /**
     * Clear isolation directory.
     */
    @After
    public void after() {
        // clear all child contents
        for (File child : isolation.listFiles()) {
            delete(child);
        }
    }

    /**
     * Delete isolation directory.
     */
    @AfterClass
    public static void afterClass() {
        delete(isolation);

        // restore original location
        FileSystem.temporaries = original;
    }

    /**
     * Helper method to delete file.
     * 
     * @param file
     */
    private static void delete(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child);
            }
        }
        file.delete();
    }

    /**
     * Create resource file which is assured that the file exists.
     * 
     * @param name
     * @return
     */
    protected File createResourceFile(String name) {
        File file = I.locate("src/test/resources/ezbean/io/" + name);

        // assert
        assertTrue(file.exists());
        assertTrue(file.isFile());

        // API definition
        return file;
    }

    /**
     * * Create resource file which is not assured that the file exists.
     * 
     * @param name
     * @return
     */
    protected File createResourceFileWithNoAssert(String name) {
        return I.locate("src/test/resources/ezbean/io/" + name);
    }

    /**
     * Create resource directory which is assured that the directory exists.
     * 
     * @param name
     * @return
     */
    protected File createResourceDirectory(String name) {
        File file = I.locate("src/test/resources/ezbean/io/" + name);

        // assert
        assertTrue(file.exists());
        assertTrue(file.isDirectory());

        // API definition
        return file;
    }

    /**
     * Create test file which is assured that the file exists.
     * 
     * @param name
     * @return
     */
    protected File createPresentTestFile(String name) {
        File file = I.locate(isolation, name);

        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        // assert
        assertTrue(file.exists());

        // API definition
        return file;
    }

    /**
     * Create test file which is not assured that the file exists.
     * 
     * @param name
     * @return
     */
    protected File createAbsentTestFile(String name) {
        File file = I.locate(isolation, name);

        if (file.exists()) {
            delete(file);
        }

        // assert
        assertFalse(file.exists());

        // API definition
        return file;
    }

    /**
     * Create test directory which is assured that the directory exists.
     * 
     * @param name
     * @return
     */
    protected File createPresentTestDirectory(String name) {
        File file = I.locate(isolation, name);

        if (!file.exists()) {
            file.mkdirs();
        }

        // assert
        assertTrue(file.exists());

        // API definition
        return file;
    }

    /**
     * Create test directory which is not assured that the directory exists.
     * 
     * @param name
     * @return
     */
    protected File createAbsentTestDirectory(String name) {
        File file = I.locate(isolation, name);

        if (file.exists()) {
            delete(file);
        }

        // assert
        assertFalse(file.exists());

        // API definition
        return file;
    }

    /**
     * Helper method to assert file.
     * 
     * @param children
     * @param expectedSize
     * @param expectedNames
     */
    protected static void assertChildren(String[] children, int expectedSize, String... expectedNames) {
        assertNotNull(children);
        assertEquals(expectedSize, children.length);

        // create name list
        List<String> list = new ArrayList();

        for (String name : expectedNames) {
            list.add(name);
        }

        // assert children
        for (String file : children) {
            assertTrue(list.remove(file));
        }
        assertEquals(0, list.size());
    }

    /**
     * Helper method to assert file.
     * 
     * @param children
     * @param expectedSize
     * @param expectedNames
     */
    protected static void assertChildren(File[] children, int expectedSize, String... expectedNames) {
        assertNotNull(children);
        assertEquals(expectedSize, children.length);

        // create name list
        List<String> list = new ArrayList();

        for (String name : expectedNames) {
            list.add(name);
        }

        // assert children
        for (File file : children) {
            assertTrue(list.remove(file.getName()));
        }
        assertEquals(0, list.size());
    }

    /**
     * Helper method to retrieve named file.
     * 
     * @param files
     * @param name
     * @return
     */
    protected static File getFile(File[] files, String name) {
        for (File file : files) {
            if (name.equals(file.getName())) {
                return file;
            }
        }
        return null;
    }
}
