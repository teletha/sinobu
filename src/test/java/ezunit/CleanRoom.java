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
import static java.nio.file.StandardCopyOption.*;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    /** The all generated {@link VirtualFile}. */
    private final ArrayList<VirtualFile> virtuals = new ArrayList();

    /**
     * Create a clean room for the current directory.
     */
    public CleanRoom() {
        this((Path) null);
    }

    /**
     * Create a clean room for the directory that the specified path indicates.
     * 
     * @param relativePath A relative location path you want to use.
     */
    public CleanRoom(String relativePath) {
        this(I.locate(relativePath));
    }

    /**
     * Create a clean room for the directory that the specified path indicates.
     * 
     * @param path A relative location path you want to use.
     */
    public CleanRoom(Path path) {
        Path directory = locatePackage(speculateInstantiator());

        if (path != null) {
            if (path.isAbsolute()) {
                directory = path;
            } else {
                directory = directory.resolve(path);
            }
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
     * <p>
     * Locate a present resource file which is assured that the spcified file exists.
     * </p>
     * 
     * @param name A file name.
     * @return A located present file.
     */
    public VirtualFile locateVirtualFile(String name) {
        VirtualFile file = new VirtualFile(locate(name, true, true));

        // Record new virtual file.
        virtuals.add(file);

        // API definition
        return file;
    }

    /**
     * <p>
     * Locate an absent resource which is assured that the specified resource doesn't exists.
     * </p>
     * 
     * @param name A resource name.
     * @return A located absent file system resource.
     */
    public VirtualFile locateVirtual(String name) {
        VirtualFile file = new VirtualFile(locate(name, false, false));

        // Record new virtual file.
        virtuals.add(file);

        // API definition
        return file;
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

            // copy all resources newly
            copyDirectory(host, root);

            // reset
            monitor.modified = false;
        }
    }

    /**
     * @see ezunit.Sandbox#after(java.lang.reflect.Method)
     */
    @Override
    protected void after(Method method) {
        try {
            validate();
        } finally {
            super.after(method);
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
     * <p>
     * Vlidate all declarations.
     * </p>
     */
    final void validate() {
        try {
            for (VirtualFile virtual : virtuals) {
                virtual.validate();
            }
        } finally {
            virtuals.clear(); // clear all virtuals
        }
    }

    /**
     * <p>
     * Helper method to copy all resource in the specified directory.
     * </p>
     * 
     * @param input A input directory.
     * @param output An output directory.
     * @throws IOException I/O error.
     */
    private void copyDirectory(Path input, Path output) throws IOException {
        Files.createDirectories(output);

        for (Path path : Files.newDirectoryStream(input, monitor)) {
            if (Files.isDirectory(path)) {
                copyDirectory(path, output.resolve(path.getFileName()));
            } else {
                copyFile(path, output.resolve(path.getFileName()));
            }
        }
    }

    /**
     * <p>
     * Copy a input file to an output file. You can override this method to change file copy
     * behavior.
     * </p>
     * 
     * @param input A input file. (not directory)
     * @param output An output file. (not directory)
     * @throws IOException I/O error.
     */
    protected void copyFile(Path input, Path output) throws IOException {
        Files.copy(input, output, COPY_ATTRIBUTES);
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
     * @version 2011/03/12 12:44:19
     */
    public static class VirtualFile {

        final Path path;

        /** The all declared file states. */
        private final List<FutureState> states = new ArrayList();

        /**
         * Invisible constructor.
         */
        private VirtualFile(Path path) {
            this.path = path;
        }

        /**
         * <p>
         * Assert the followings.
         * </p>
         * <ul>
         * <li>This file exists now.</li>
         * <li>This file will be deleted after test.</li>
         * </ul>
         */
        public void willBeDeleted() {
            // now
            assertTrue(Files.exists(path));

            // future
            states.add(new FutureState(new Not(new Exist()), "'" + path + "' must be deleted after test."));
        }

        /**
         * <p>
         * Assert the followings.
         * </p>
         * <ul>
         * <li>This file don't exist now.</li>
         * <li>This file will be created after test.</li>
         * </ul>
         */
        public void willBeCreated() {
            // now
            assertTrue(Files.notExists(path));

            // future
            states.add(new FutureState(new Exist(), "'" + path + "' must be created after test."));
        }

        /**
         * <p>
         * Validate all registered states.
         * </p>
         */
        private void validate() {
            for (FutureState state : states) {
                if (!state.validator.validate(path)) {
                    throw state;
                }
            }
        }
    }

    /**
     * @version 2011/03/12 12:46:44
     */
    @SuppressWarnings("serial")
    private static class FutureState extends IllegalStateException {

        /** The file state validator. */
        private final Validator validator;

        /**
         * @param message
         */
        private FutureState(Validator validator, String message) {
            super(message);

            this.validator = validator;

            // Hide unnecessary stack traces.
            StackTraceElement[] elements = getStackTrace();
            setStackTrace(Arrays.copyOfRange(elements, 1, elements.length));
        }
    }

    /**
     * @version 2011/03/12 13:07:34
     */
    private static interface Validator {

        /**
         * <p>
         * Validate the specified path state.
         * <p>
         * 
         * @param path A target path.
         * @return A result.
         */
        boolean validate(Path path);
    }

    /**
     * @version 2011/03/12 13:08:56
     */
    private static class Exist implements Validator {

        /**
         * @see ezunit.CleanRoom.Validator#validate(java.nio.file.Path)
         */
        public boolean validate(Path path) {
            return Files.exists(path);
        }
    }

    /**
     * @version 2011/03/12 13:19:05
     */
    private static class Not implements Validator {

        /** The actual. */
        private final Validator validator;

        /**
         * @param validator
         */
        private Not(Validator validator) {
            this.validator = validator;
        }

        /**
         * @see ezunit.CleanRoom.Validator#validate(java.nio.file.Path)
         */
        public boolean validate(Path path) {
            return !validator.validate(path);
        }
    }
}
