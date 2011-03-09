/*
 * Copyright (C) 2011 Nameless Production Committee.
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

import static java.nio.file.FileVisitResult.*;
import static java.nio.file.StandardCopyOption.*;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import ezbean.I;

/**
 * @version 2011/03/07 17:16:07
 */
public final class Filer implements FileVisitor<Path> {

    /** The root temporary directory for Ezbean. */
    private static Path temporaries;

    /** The temporary directory for the current processing JVM. */
    private static Path temporary;

    // initialize
    static {
        try {
            temporaries = Paths.get(System.getProperty("java.io.tmpdir"), "Ezbean");
            Files.createDirectories(temporaries);

            // Clean up any old temporary directories by listing all of the files, using a prefix
            // filter and that don't have a lock file.
            for (Path path : Files.newDirectoryStream(temporaries, "glob:temporary*")) {
                // create a file to represent the lock and test
                RandomAccessFile lock = new RandomAccessFile(path.resolve("lock").toFile(), "rw");

                // delete the contents of the temporary directory since it can retrieve a
                // exclusive lock
                if (lock.getChannel().tryLock() != null) {
                    // release lock at first
                    lock.close();

                    // delete actually
                    Files.deleteIfExists(path);
                }
            }

            // Create the temporary directory for the current processing JVM.
            temporary = Files.createTempDirectory(temporaries, "temporary");

            // Create a lock after creating the temporary directory so there is no race condition
            // with another application trying to clean our temporary directory.
            new RandomAccessFile(temporary.resolve("lock").toFile(), "rw").getChannel().tryLock();
        } catch (SecurityException e) {
            temporary = null;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /** The base path. */
    private final Path base;

    private final int size;

    /** The actual vistor to delegate. */
    private final FileVisitor<Path> visitor;

    /** The include patterns. */
    private final ArrayList<PathMatcher> includes = new ArrayList();

    /** The exclude patterns. */
    private final ArrayList<PathMatcher> excludes = new ArrayList();

    /**
     * @param base
     */
    private Filer(Path base, String[] patterns, FileVisitor<Path> visitor) throws IOException {
        this.base = base;
        this.visitor = visitor;
        this.size = base.getNameCount();

        FileSystem system = base.getFileSystem();

        for (String pattern : patterns) {
            if (pattern.charAt(0) == '!') {
                // exclude pattern
                excludes.add(system.getPathMatcher("glob:".concat(pattern.substring(1))));
            } else {
                // include pattern
                includes.add(system.getPathMatcher("glob:".concat(pattern)));
            }
        }

        Files.walkFileTree(base, this);
    }

    /**
     * @see java.nio.file.FileVisitor#preVisitDirectory(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        return visitor.preVisitDirectory(dir, attrs);
    }

    /**
     * @see java.nio.file.FileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
     */
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return visitor.postVisitDirectory(dir, exc);
    }

    /**
     * @see java.nio.file.FileVisitor#visitFile(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Path relative = file.subpath(size, file.getNameCount());

        for (PathMatcher matcher : excludes) {
            if (matcher.matches(relative)) {
                return FileVisitResult.CONTINUE;
            }
        }

        for (PathMatcher matcher : includes) {
            if (matcher.matches(relative)) {
                return visitor.visitFile(file, attrs);
            }
        }
        return !includes.isEmpty() ? FileVisitResult.CONTINUE : visitor.visitFile(file, attrs);
    }

    /**
     * @see java.nio.file.FileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
     */
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return visitor.visitFileFailed(file, exc);
    }

    public static void walk(Path base, String[] patterns, FileVisitor<Path> visitor) {
        try {
            Files.walkFileTree(base, new Filer(base, patterns, visitor));
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Copy all file in this {@link Path} to the specified {@link Path}.
     * </p>
     * 
     * @param input
     * @param output
     * @param patterns
     * @return
     * @throws NullPointerException If the input or output file is <code>null</code>.
     * @throws NoSuchFileException If the input file does not exist.
     */
    public static void copy(Path input, Path output, String... patterns) {
        try {
            if (isDirectory(input)) {
                new Filer(input, patterns, new Operation(input, output, true));
            } else {
                if (isDirectory(output)) {
                    output = output.resolve(input.getFileName());
                }

                // Assure the existence of the parent directory.
                Files.createDirectories(output.getParent());

                // Copy file actually.
                Files.copy(input, output, COPY_ATTRIBUTES, REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Copy all file in this {@link Path} to the specified {@link Path}.
     * </p>
     * 
     * @param input
     * @param output
     * @param patterns
     * @return
     * @throws NullPointerException If the input or output file is <code>null</code>.
     * @throws NoSuchFileException If the input file does not exist.
     */
    public static void move(Path input, Path output, String... patterns) {
        try {
            if (isDirectory(input)) {
                new Filer(input, patterns, new Operation(input, output, false));
            } else {
                if (isDirectory(output)) {
                    output = output.resolve(input.getFileName());
                }

                // Assure the existence of the parent directory.
                Files.createDirectories(output.getParent());

                // Copy file actually.
                Files.move(input, output, ATOMIC_MOVE, REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Copy all file in this {@link Path} to the specified {@link Path}.
     * </p>
     * 
     * @param intput
     * @param patterns
     * @throws NullPointerException If the input file is <code>null</code>.
     * @throws NoSuchFileException If the input file does not exist.
     */
    public static void delete(Path intput, String... patterns) {
        if (intput != null) {
            try {
                if (Files.isDirectory(intput)) {
                    new Filer(intput, patterns, new Operation(intput, null, false));
                } else {
                    Files.deleteIfExists(intput);
                }
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * <p>
     * Creates a new directory. The check for the existence of the file and the creation of the
     * directory if it does not exist are a single operation that is atomic with respect to all
     * other filesystem activities that might affect the directory. This method should be used where
     * it is required to create all nonexistent parent directories first.
     * </p>
     * 
     * @param path
     */
    public static void createDirectory(Path path) {
        if (path != null && !exist(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * <p>
     * Tests whether a file exists.
     * </p>
     * 
     * @param path
     * @return
     */
    public static boolean exist(Path path) {
        return path == null ? false : Files.exists(path);
    }

    /**
     * <p>
     * Tests if two paths locate the same file.
     * </p>
     * 
     * @param one
     * @param other
     * @return
     */
    public static boolean isSame(Path one, Path other) {
        try {
            return Files.isSameFile(one, other);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Tests whether a file is a regular file with opaque content.
     * </p>
     * 
     * @param path
     * @return
     */
    public static boolean isFile(Path path) {
        return path == null ? false : Files.isRegularFile(path);
    }

    /**
     * <p>
     * Tests whether a file is a regular directory with opaque content.
     * </p>
     * 
     * @param path
     * @return
     */
    public static boolean isDirectory(Path path) {
        return path == null ? false : Files.isDirectory(path);
    }

    /**
     * <p>
     * Creates a new abstract file somewhere beneath the system's temporary directory (as defined by
     * the <code>java.io.tmpdir</code> system property).
     * </p>
     * <p>
     * </p>
     * 
     * @return A newly created temporary file which is not exist yet.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static Path createTemporaryFile() {
        try {
            return Files.createTempDirectory(temporary, "temporary");
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @version 2011/02/16 12:19:34
     */
    private static final class Operation extends SimpleFileVisitor<Path> {

        /** The source location. */
        private final Path from;

        /** The target location. */
        private final Path to;

        /** The sccess flag. */
        private boolean success = true;

        /** The option to remain original file. */
        private final boolean remain;

        /**
         * @param from
         * @param to
         */
        private Operation(Path from, Path to, boolean remain) {
            this.from = from.getParent();
            this.to = to;
            this.remain = remain;
        }

        /**
         * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object,
         *      java.nio.file.attribute.BasicFileAttributes)
         */
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (to != null) {
                createDirectory(to.resolve(from.relativize(dir)));
            }
            return CONTINUE;
        }

        /**
         * @see java.nio.file.SimpleFileVisitor#postVisitDirectory(java.lang.Object,
         *      java.io.IOException)
         */
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (to != null) {
                Files.setLastModifiedTime(to.resolve(from.relativize(dir)), Files.getLastModifiedTime(dir));
            }

            if (!remain) {
                Files.delete(dir);
            }
            return super.postVisitDirectory(dir, exc);
        }

        /**
         * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object,
         *      java.nio.file.attribute.BasicFileAttributes)
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (to != null) {
                Files.copy(file, to.resolve(from.relativize(file)), COPY_ATTRIBUTES, REPLACE_EXISTING);
            }

            if (!remain) {
                Files.delete(file);
            }

            return CONTINUE;
        }
    }
}
