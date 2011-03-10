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
package ezbean;

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

/**
 * @version 2011/03/07 17:16:07
 */
public final class Filer extends SimpleFileVisitor<Path> {

    /** The root temporary directory for Ezbean. */
    private static Path temporaries;

    /** The temporary directory for the current processing JVM. */
    private static Path temporary;

    // initialize
    static {
        try {
            // Create the root temporary directory for Ezbean.
            temporaries = Files.createDirectories(Paths.get(System.getProperty("java.io.tmpdir"), "Ezbean"));

            // Clean up any old temporary directories by listing all of the files, using a prefix
            // filter and that don't have a lock file.
            for (Path path : Files.newDirectoryStream(temporaries, "temporary*")) {
                // create a file to represent the lock and test
                RandomAccessFile lock = new RandomAccessFile(path.resolve("lock").toFile(), "rw");

                // delete the contents of the temporary directory since it can retrieve a
                // exclusive lock
                if (lock.getChannel().tryLock() != null) {
                    // release lock at first
                    lock.close();

                    // delete actually
                    delete(path);
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

    /** The actual {@link FileVisitor} to delegate. */
    private final FileVisitor<Path> visitor;

    /** The flag whether we should sort out directories or not. */
    private final boolean directory;

    /** The flag whether we should sort out files or not. */
    private final boolean file;

    /** The include file patterns. */
    private final PathMatcher[] includes;

    /** The exclude file patterns. */
    private final PathMatcher[] excludes;

    /** The exclude directory pattern. */
    private final PathMatcher[] excludeDirectories;

    /** The prefix size of root path. */
    private final int start;

    /**
     * @param visitor
     * @param base
     * @param patterns
     */
    public Filer(Path base, FileVisitor<Path> visitor, String... patterns) {
        this.visitor = visitor;
        this.start = base.getNameCount();

        FileSystem system = base.getFileSystem();
        ArrayList<PathMatcher> includes = new ArrayList();
        ArrayList<PathMatcher> excludes = new ArrayList();
        ArrayList<PathMatcher> excludeDirectories = new ArrayList();

        for (String pattern : patterns) {
            if (pattern.charAt(0) != '!') {
                // include
                includes.add(system.getPathMatcher("glob:".concat(pattern)));
            } else {
                // exclude
                if (pattern.endsWith("/**")) {
                    // directory match
                    excludeDirectories.add(system.getPathMatcher("glob:".concat(pattern.substring(1, pattern.length() - 3))));
                } else {
                    // anything match
                    excludes.add(system.getPathMatcher("glob:".concat(pattern.substring(1))));
                }
            }
        }

        // Convert into Array
        this.includes = includes.toArray(new PathMatcher[includes.size()]);
        this.excludes = excludes.toArray(new PathMatcher[excludes.size()]);
        this.excludeDirectories = excludeDirectories.toArray(new PathMatcher[excludeDirectories.size()]);
        this.file = this.includes.length != 0 || this.excludes.length != 0;
        this.directory = this.excludeDirectories.length != 0;

        try {
            Files.walkFileTree(base, this);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @see java.nio.file.FileVisitor#preVisitDirectory(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
        if (directory) {
            // We must skip root directory.
            int end = path.getNameCount();

            if (start != end) {
                // Retrieve relative path from base.
                Path relative = path.subpath(start, end);

                // Directory exclusion make fast traversing file tree.
                for (PathMatcher matcher : excludeDirectories) {
                    if (matcher.matches(relative)) {
                        return SKIP_SUBTREE;
                    }
                }
            }
        }

        // API definition
        return visitor.preVisitDirectory(path, attrs);
    }

    /**
     * @see java.nio.file.FileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
     */
    @Override
    public FileVisitResult postVisitDirectory(Path path, IOException exc) throws IOException {
        return visitor.postVisitDirectory(path, exc);
    }

    /**
     * @see java.nio.file.FileVisitor#visitFile(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        if (file) {
            // Retrieve relative path from base.
            Path relative = path.subpath(start, path.getNameCount());

            // File exclusion
            for (PathMatcher matcher : excludes) {
                if (matcher.matches(relative)) {
                    return CONTINUE;
                }
            }

            if (includes.length != 0) {
                // File inclusion
                for (PathMatcher matcher : includes) {
                    if (matcher.matches(relative)) {
                        return visitor.visitFile(path, attrs);
                    }
                }

                // API definition
                return CONTINUE;
            }
        }

        // API definition
        return visitor.visitFile(path, attrs);
    }

    public static void walk(Path base, String[] patterns, FileVisitor<Path> visitor) {
        try {
            Files.walkFileTree(base, new Filer(base, visitor, patterns));
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Generic method to copy a input {@link Path} to an output {@link Path} deeply.
     * </p>
     * 
     * @param input A input {@link Path} object which can be file or directory.
     * @param output An outout {@link Path} object which can be file or directory.
     * @throws NullPointerException If the specified input or output file is <code>null</code>.
     * @throws IOException If an I/O error occurs.
     * @throws NoSuchFileException If the specified input file is not found. If the input file is
     *             directory and the output file is <em>not</em> directory.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static void copy(Path input, Path output, String... patterns) {
        try {
            if (isDirectory(input)) {
                new Filer(input, new Operation(input, output, 0), patterns);
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
                new Filer(input, new Operation(input, output, 1), patterns);
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
                    new Filer(intput, new Operation(intput, null, 2), patterns);
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
    public static Path createTemporary() {
        try {
            Path path = Files.createTempDirectory(temporary, "temporary");

            // Delete entity file.
            Files.delete(path);

            // API definition
            return path;
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }
}
