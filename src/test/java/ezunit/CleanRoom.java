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
package ezunit;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileFilter;
import java.lang.reflect.Method;
import java.nio.charset.Charset;

import ezbean.I;
import ezbean.io.FileSystem;

/**
 * <p>
 * The environmental rule for test that depends on file system.
 * </p>
 * 
 * @version 2010/02/10 18:46:50
 */
public class CleanRoom extends Sandbox {

    /** The root bioclean room for tests which are related with file system. */
    private static final File clean = new File(I.getWorkingDirectory(), "clean-room");

    /** The host directory for test. */
    private final File host;

    /** The clean room monitor. */
    private final Monitor monitor = new Monitor();

    /**
     * Create a clean room for the current directory.
     */
    public CleanRoom() {
        this((File) null);
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
            directory = Ezunit.locatePackage(UnsafeUtility.speculateInstantiator());
        }

        if (!directory.isDirectory()) {
            directory = directory.getParentFile();
        }
        this.host = directory;

        // access control
        writable(false, host);
    }

    /**
     * <p>
     * Assume platform encoding.
     * </p>
     * 
     * @param charset Your exepcted charcter encoding.
     */
    public void assume(Charset charset) {
        assumeThat(Charset.defaultCharset(), is(charset));
    }

    /**
     * <p>
     * Assume platform encoding.
     * </p>
     * 
     * @param charset Your exepcted charcter encoding.
     */
    public void assume(String charset) {
        assumeThat(Charset.defaultCharset(), is(Charset.forName(charset)));
    }

    /**
     * <p>
     * Locate a present resource file which is assured that the spcified file exists.
     * </p>
     * 
     * @param name A file name.
     * @return A located present file.
     */
    public File locateFile(String name) {
        return locate(name, true, true);
    }

    /**
     * <p>
     * Locate a present resource directory which is assured that the specified directory exists.
     * </p>
     * 
     * @param name A directory name.
     * @return A located present directory.
     */
    public File locateDirectory(String name) {
        return locate(name, true, false);
    }

    /**
     * <p>
     * Locate an absent resource which is assured that the specified resource doesn't exists.
     * </p>
     * 
     * @param name A resource name.
     * @return A located absent file system resource.
     */
    public File locateAbsent(String name) {
        return locate(name, false, false);
    }

    /**
     * Helper method to locate file in clean room.
     * 
     * @param path
     * @return
     */
    private File locate(String path, boolean isPresent, boolean isFile) {
        // null check
        if (path == null) {
            path = "";
        }

        // locate virtual file in the clean room
        File virtual = I.locate(clean, path);

        assertEquals(virtual.exists(), isPresent);
        assertEquals(virtual.isFile(), isFile);

        return virtual;
    }

    /**
     * @see ezunit.EzRule#before(java.lang.reflect.Method)
     */
    @Override
    protected void before(Method method) throws Exception {
        super.before(method);

        // start monitoring clean room
        use(monitor);

        // renew clean room for this test if needed
        if (monitor.modified) {
            clean.mkdirs();

            // clean up all resources
            FileSystem.clear(clean);

            // copy all resources newly
            for (File file : host.listFiles(monitor)) {
                FileSystem.copy(file, clean, monitor);
            }

            // reset
            monitor.modified = false;
        }
    }

    /**
     * @see ezunit.EzRule#afterClass()
     */
    @Override
    protected void afterClass() {
        // dispose clean room actually
        delete(clean);

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

    /**
     * @version 2010/02/13 13:23:22
     */
    private class Monitor extends Security implements FileFilter {

        /** The flag for file resource modification. */
        private boolean modified = true;

        /**
         * @see ezunit.Sandbox.Security#checkDelete(java.lang.String)
         */
        @Override
        public void checkDelete(String file) {
            if (!modified && file.startsWith(clean.getPath())) {
                modified = true;
            }
        }

        /**
         * @see ezunit.Sandbox.Security#checkWrite(java.io.FileDescriptor)
         */
        @Override
        public void checkWrite(FileDescriptor fd) {
            if (!modified && fd.toString().startsWith(clean.getPath())) {
                modified = true;
            }
        }

        /**
         * @see ezunit.Sandbox.Security#checkWrite(java.lang.String)
         */
        @Override
        public void checkWrite(String file) {
            if (!modified && file.startsWith(clean.getPath())) {
                modified = true;
            }
        }

        /**
         * @see java.io.FileFilter#accept(java.io.File)
         */
        public boolean accept(File file) {
            String name = file.getName();

            return !name.equals("package-info.html") && !name.endsWith(".class");
        }
    }
}
