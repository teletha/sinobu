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
package ezbean.io;

import static java.nio.file.FileVisitResult.*;
import static java.nio.file.StandardCopyOption.*;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import ezbean.I;

/**
 * @version 2011/03/07 17:16:07
 */
public final class Filer implements FileVisitor<Path> {

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
     * @param from
     * @param to
     * @param patterns
     * @return
     */
    public static void copy(Path from, Path to, String... patterns) {
        try {
            if (isFile(from)) {
                if (isDirectory(to)) {
                    to = to.resolve(from.getFileName());
                }

                // Assure the existence of the parent directory.
                Files.createDirectories(to.getParent());

                // Copy file actually.
                Files.copy(from, to, COPY_ATTRIBUTES, REPLACE_EXISTING);
            } else if (isDirectory(from) && !isFile(to)) {
                new Filer(from, patterns, new Copy(from, to));
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
     * @param from
     * @param to
     * @param patterns
     * @return
     */
    public static boolean move(Path from, Path to, String... patterns) {
        return true;
    }

    /**
     * <p>
     * Copy all file in this {@link Path} to the specified {@link Path}.
     * </p>
     * 
     * @param from
     * @param patterns
     * @return
     */
    public static boolean delete(Path from, String... patterns) {
        return true;
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
     * @version 2011/02/16 12:19:34
     */
    private static final class Copy extends SimpleFileVisitor<Path> {

        /** The source location. */
        private final Path from;

        /** The target location. */
        private final Path to;

        /** The sccess flag. */
        private boolean success = true;

        /**
         * @param from
         * @param to
         */
        private Copy(Path from, Path to) {
            this.from = from.getParent();
            this.to = to;
        }

        /**
         * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object,
         *      java.nio.file.attribute.BasicFileAttributes)
         */
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            // System.out.println(dir + "   " + to.resolve(from.relativize(dir)));
            createDirectory(to.resolve(from.relativize(dir)));

            return CONTINUE;
        }

        /**
         * @see java.nio.file.SimpleFileVisitor#postVisitDirectory(java.lang.Object,
         *      java.io.IOException)
         */
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            Files.setLastModifiedTime(to.resolve(from.relativize(dir)), Files.getLastModifiedTime(dir));
            return super.postVisitDirectory(dir, exc);
        }

        /**
         * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object,
         *      java.nio.file.attribute.BasicFileAttributes)
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.copy(file, to.resolve(from.relativize(file)), COPY_ATTRIBUTES, REPLACE_EXISTING);

            return CONTINUE;
        }
    }
}
