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
package ezunit;

import static ezunit.Ezunit.*;
import static ezunit.UnsafeUtility.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.junit.Assume.*;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;

import ezbean.I;
import ezbean.io.FilePath;

/**
 * <p>
 * The environmental rule for test that depends on file system.
 * </p>
 * 
 * @version 2011/03/07 12:36:48
 */
public class CleanRoom extends Sandbox {

    /** The counter for instances. */
    private static final AtomicInteger counter = new AtomicInteger();

    /** The root bioclean room for tests which are related with file system. */
    private static final FilePath clean = I.locate("target/clean-room");

    /** The temporary bioclean room for this instance which are related with file system. */
    public final FilePath root = I.locate(clean, String.valueOf(counter.incrementAndGet()));

    /** The host directory for test. */
    private final FilePath host;

    /** The root bioclean room for tests which are related with file system. */
    private static final Path clean2 = Paths.get("target/clean-room");

    /** The temporary bioclean room for this instance which are related with file system. */
    public final Path root2 = clean2.resolve(root.getName());

    /** The host directory for test. */
    private final Path host2;

    /** The clean room monitor. */
    private final Monitor monitor = new Monitor();

    /**
     * Create a clean room for the current directory.
     */
    public CleanRoom() {
        this(null);
    }

    /**
     * Create a clean room for the directory that the specified path indicates.
     * 
     * @param path A directory location you want to use.
     */
    public CleanRoom(String path) {
        FilePath directory = path == null ? locatePackage(speculateInstantiator()) : I.locate(path);

        if (!directory.isDirectory()) {
            directory = directory.getParentFile();
        }
        this.host = directory;

        // access control
        writable(false, host);

        Path directory2 = path == null ? locatePackage2(speculateInstantiator()) : Paths.get(path);

        if (!Files.isDirectory(directory2)) {
            directory2 = directory2.getParent();
        }
        this.host2 = directory2;
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
    public FilePath locateFile(String name) {
        return locate(name, true, true);
    }

    /**
     * <p>
     * Locate a present resource file which is assured that the spcified file exists.
     * </p>
     * 
     * @param name A file name.
     * @return A located present file.
     */
    public Path locateFile2(String name) {
        return locate2(name, true, true);
    }

    /**
     * <p>
     * Locate a present resource directory which is assured that the specified directory exists.
     * </p>
     * 
     * @param name A directory name.
     * @return A located present directory.
     */
    public FilePath locateDirectory(String name) {
        return locate(name, true, false);
    }

    /**
     * <p>
     * Locate a present resource directory which is assured that the specified directory exists.
     * </p>
     * 
     * @param name A directory name.
     * @return A located present directory.
     */
    public Path locateDirectory2(String name) {
        return locate2(name, true, false);
    }

    /**
     * <p>
     * Locate an absent resource which is assured that the specified resource doesn't exists.
     * </p>
     * 
     * @param name A resource name.
     * @return A located absent file system resource.
     */
    public FilePath locateAbsent(String name) {
        return locate(name, false, false);
    }

    /**
     * <p>
     * Locate an absent resource which is assured that the specified resource doesn't exists.
     * </p>
     * 
     * @param name A resource name.
     * @return A located absent file system resource.
     */
    public Path locateAbsent2(String name) {
        return locate2(name, false, false);
    }

    /**
     * Helper method to locate file in clean room.
     * 
     * @param path
     * @return
     */
    private FilePath locate(String path, boolean isPresent, boolean isFile) {
        // null check
        if (path == null) {
            path = "";
        }

        // locate virtual file in the clean room
        FilePath virtual = I.locate(root, path);

        // create virtual file if needed
        if (isPresent) {
            if (isFile) {
                try {
                    virtual.getParentFile().mkdirs();
                    virtual.createNewFile();
                } catch (IOException e) {
                    throw I.quiet(e);
                }
            } else {
                virtual.mkdirs();
            }
        }

        // validate file state
        assertEquals(virtual.exists(), isPresent);
        assertEquals(virtual.isFile(), isFile);

        // API definition
        return virtual;
    }

    /**
     * Helper method to locate file in clean room.
     * 
     * @param path
     * @return
     */
    private Path locate2(String path, boolean isPresent, boolean isFile) {
        // null check
        if (path == null) {
            path = "";
        }

        // locate virtual file in the clean room
        Path virtual = root2.resolve(path);

        // create virtual file if needed
        if (isPresent) {
            if (isFile) {
                // create parent directory
                try {
                    Files.createDirectories(virtual.getParent());
                } catch (IOException e) {
                    throw I.quiet(e);
                }

                // create requested file
                try {
                    Files.createFile(virtual);
                } catch (FileAlreadyExistsException e) {
                    // ignore
                } catch (IOException e) {
                    throw I.quiet(e);
                }
            } else {
                // create requested directory
                try {
                    Files.createDirectories(virtual);
                } catch (IOException e) {
                    throw I.quiet(e);
                }
            }
        }

        // validate file state
        assertEquals(Files.exists(virtual), isPresent);
        assertEquals(Files.isRegularFile(virtual), isFile);

        // API definition
        return virtual;
    }

    /**
     * @see ezunit.ReusableRule#before(java.lang.reflect.Method)
     */
    @Override
    protected void before(Method method) throws Exception {
        super.before(method);

        // start monitoring clean room
        use(monitor);

        // renew clean room for this test if needed
        if (monitor.modified) {
            root.mkdirs();

            // clean up all resources
            root.delete();
            root.mkdir();

            // copy all resources newly
            for (File file : host.listFiles(monitor)) {
                Filer.copy(file.toPath(), root.toPath()); // TODO filter
            }

            // reset
            monitor.modified = false;
        }
    }

    /**
     * @see ezunit.ReusableRule#afterClass()
     */
    @Override
    protected void afterClass() {
        // dispose clean room actually
        delete(root);

        // check clean room usage
        if (clean.list().length == 0) {
            delete(clean);
        }

        // delegate
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
            if (!modified && file.startsWith(root.getPath())) {
                modified = true;
            }
        }

        /**
         * @see ezunit.Sandbox.Security#checkWrite(java.io.FileDescriptor)
         */
        @Override
        public void checkWrite(FileDescriptor fd) {
            if (!modified && fd.toString().startsWith(root.getPath())) {
                modified = true;
            }
        }

        /**
         * @see ezunit.Sandbox.Security#checkWrite(java.lang.String)
         */
        @Override
        public void checkWrite(String file) {
            if (!modified && file.startsWith(root.getPath())) {
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

    /**
     * @version 2011/03/09 21:44:23
     */
    public static abstract class VirtualFile {

        /**
         * <p>
         * Build virtual files.
         * </p>
         * 
         * @param root
         */
        private void build(Path root) {

        }
    }
}
