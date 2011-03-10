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

import java.io.FileDescriptor;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.atomic.AtomicInteger;

import ezbean.I;

/**
 * <p>
 * The environmental rule for test that depends on file system.
 * </p>
 * 
 * @version 2011/03/10 9:59:21
 */
public class CleanRoom extends Sandbox {

    /** The counter for instances. */
    private static final AtomicInteger counter = new AtomicInteger();

    /** The root bioclean room for tests which are related with file system. */
    private static final Path clean = Paths.get("target/clean-room");

    /** The temporary bioclean room for this instance which are related with file system. */
    public final Path root = clean.resolve(String.valueOf(counter.incrementAndGet()));

    /** The host directory for test. */
    private final Path host;

    /** The clean room monitor. */
    private final Monitor monitor = new Monitor(root);

    /**
     * Create a clean room for the current directory.
     */
    public CleanRoom() {
        this(null);
    }

    /**
     * Create a clean room for the directory that the specified path indicates.
     * 
     * @param path A relative location path you want to use.
     */
    public CleanRoom(String path) {
        Path directory = locatePackage(speculateInstantiator());

        if (path != null) {
            directory = directory.resolve(path);
        }

        if (!Files.isDirectory(directory)) {
            directory = directory.getParent();
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
    public Path locateFile(String name) {
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
    public Path locateDirectory(String name) {
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
    public Path locateAbsent(String name) {
        return locate(name, false, false);
    }

    /**
     * Helper method to locate file in clean room.
     * 
     * @param path
     * @return
     */
    private Path locate(String path, boolean isPresent, boolean isFile) {
        // null check
        if (path == null) {
            path = "";
        }

        // locate virtual file in the clean room
        Path virtual = root.resolve(path);

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
            // clean up all resources
            sweep(root);

            // create actual clean room
            Files.createDirectories(root);

            // copy all resources newly
            for (Path path : Files.newDirectoryStream(host, monitor)) {
                I.copy(path, root, "!**.class"); // TODO filter
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
        // Dispose clean room actually.
        sweep(root);

        // Delete root directory of clean room.
        try {
            Files.delete(clean);
        } catch (DirectoryNotEmptyException e) {
            // CleanRoom is used by other testcase, So we can't delete.
        } catch (IOException e) {
            throw I.quiet(e);
        }

        // delegate
        super.afterClass();
    }

    /**
     * Helper method to delete files.
     * 
     * @param path
     */
    private void sweep(Path path) {
        if (Files.exists(path)) {
            try {
                Files.walkFileTree(path, new Sweeper());
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * @version 2011/03/10 9:35:05
     */
    private static final class Sweeper extends SimpleFileVisitor<Path> {

        /**
         * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object,
         *      java.nio.file.attribute.BasicFileAttributes)
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.deleteIfExists(file);
            return FileVisitResult.CONTINUE;
        }

        /**
         * @see java.nio.file.SimpleFileVisitor#postVisitDirectory(java.lang.Object,
         *      java.io.IOException)
         */
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.deleteIfExists(dir);
            return FileVisitResult.CONTINUE;
        }
    }

    /**
     * @version 2010/02/13 13:23:22
     */
    private class Monitor extends Security implements Filter<Path> {

        /** The path prefix. */
        private final String prefix;

        /** The flag for file resource modification. */
        private boolean modified = true;

        /**
         * @param root
         */
        public Monitor(Path root) {
            this.prefix = root.toString();
        }

        /**
         * @see ezunit.Sandbox.Security#checkDelete(java.lang.String)
         */
        @Override
        public void checkDelete(String file) {
            if (!modified && file.startsWith(prefix)) {
                modified = true;
            }
        }

        /**
         * @see ezunit.Sandbox.Security#checkWrite(java.io.FileDescriptor)
         */
        @Override
        public void checkWrite(FileDescriptor fd) {
            if (!modified && fd.toString().startsWith(prefix)) {
                modified = true;
            }
        }

        /**
         * @see ezunit.Sandbox.Security#checkWrite(java.lang.String)
         */
        @Override
        public void checkWrite(String file) {
            if (!modified && file.startsWith(prefix)) {
                modified = true;
            }
        }

        /**
         * @see java.nio.file.DirectoryStream.Filter#accept(java.lang.Object)
         */
        public boolean accept(Path path) throws IOException {
            String name = path.getFileName().toString();

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
