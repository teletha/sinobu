/*
 * Copyright (C) 2010 Nameless Production Committee.
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
package ezbean.unit;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;

import ezbean.I;
import ezbean.io.FileSystem;

/**
 * @version 2010/02/10 18:46:50
 */
public class CleanRoom extends Sandbox {

    /** The global counter for clean rooms. */
    private static final AtomicInteger counter = new AtomicInteger();

    /** The root bioclean room for tests which are related with file system. */
    private static final File cleans = FileSystem.createTemporary();

    /** The host directory for test. */
    private final File host;

    private File clean;

    /**
     * Create a clean room for the current directory.
     */
    public CleanRoom() {
        this(new File(""));
    }

    /**
     * Create a clean room for the directory that the specified path indicates.
     * 
     * @param path A directory location you want to use.
     */
    public CleanRoom(String path) {
        this(I.locate(path));
    }

    /**
     * Create a clean room for the specified directory.
     * 
     * @param directory A directory location you want to use.
     */
    public CleanRoom(File directory) {
        if (directory == null) {
            directory = new File("");
        }

        if (!directory.isDirectory()) {
            directory = directory.getParentFile();
        }
        this.host = directory;

        // access control
        writable(false, host);
    }

    /**
     * Create resource file which is assured that the file exists.
     * 
     * @param name
     * @return
     */
    public File locateFile(String name) {
        File file = I.locate(host, name);

        // assert
        assertTrue(file.exists());
        assertTrue(file.isFile());

        // API definition
        return file;
    }

    /**
     * Create test file which is assured that the file exists.
     * 
     * @param name
     * @return
     */
    public File newPresentFile(String name) {
        File file = I.locate(clean, name);

        try {
            if (!file.createNewFile()) {
                throw new AssertionError("Can't create new file.");
            }
        } catch (Exception e) {
            throw I.quiet(e);
        }

        // assert
        assertTrue(file.exists());
        assertTrue(file.isFile());

        // API definition
        return file;
    }

    /**
     * Create test file which is not assured that the file exists.
     * 
     * @param name
     * @return
     */
    public File newAbsentFile(String name) {
        File file = I.locate(clean, name);

        if (file.exists()) {
            throw new AssertionError("The file is aleady existed.");
        }

        // assert
        assertFalse(file.exists());

        // API definition
        return file;
    }

    /**
     * @see ezbean.unit.EzRule#before(java.lang.reflect.Method)
     */
    @Override
    protected void before(Method method) throws Exception {
        super.before(method);

        // create clean room for this test
        clean = new File(cleans, String.valueOf(counter.getAndIncrement()));
        clean.mkdirs();
    }

    /**
     * @see ezbean.unit.EzRule#after(java.lang.reflect.Method)
     */
    @Override
    protected void after(Method method) {
        // delete clean room for this test
        delete(clean);

        super.after(method);
    }

    /**
     * @see ezbean.unit.EzRule#afterClass()
     */
    @Override
    protected void afterClass() {
        delete(cleans);

        super.afterClass();
    }

    /**
     * Standalone method to delete file.
     * 
     * @param file
     */
    private void delete(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                delete(child);
            }
        }
        file.delete();
    }
}
