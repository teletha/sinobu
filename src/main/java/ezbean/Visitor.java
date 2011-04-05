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
import java.nio.file.FileSystem;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;

/**
 * @version 2011/03/13 0:32:21
 */
@SuppressWarnings("serial")
class Visitor extends ArrayList<Path> implements FileVisitor<Path> {

    /** The source. */
    private final Path from;

    /** The destination. */
    private final Path to;

    /** The operation type. */
    private final int type;

    /** The actual {@link FileVisitor} to delegate. */
    private final FileVisitor<Path> visitor;

    /** The include file patterns. */
    private final PathMatcher[] includes;

    /** The exclude file patterns. */
    private final PathMatcher[] excludes;

    /** The exclude directory pattern. */
    private final PathMatcher[] directories;

    /** The max file system depth. */
    private final int depth;

    /** The current file system depth. */
    private int current = 1;

    /**
     * <p>
     * Utility for file tree traversal.
     * </p>
     */
    Visitor(Path from, Path to, int type, int depth, FileVisitor visitor, String... patterns) {
        this.type = type;
        this.visitor = visitor;

        try {
            boolean directory = Files.isDirectory(from);

            // The depth of scanning.
            //
            // The {@link FileVisitor#visitFile(Object, BasicFileAttributes)} method is invoked for
            // all files, including directories, encountered at max depth. So we use the value which
            // is greater than the user specified max depth, and skip lowest files using
            // SKIP_SUBTREE value.
            this.depth = directory ? depth <= 0 ? Integer.MAX_VALUE : depth + 1 : 0;

            // The copy and move operations need the root path.
            this.from = directory && type < 2 ? from.getParent() : from;

            // The copy and move operations need destination. If the source is file, so destination
            // must be file and its name is equal to source file.
            this.to = !directory && type < 2 && Files.isDirectory(to) ? to.resolve(from.getFileName()) : to;

            if (type < 2) {
                Files.createDirectories(to.getParent());
            }

            // Parse and create path matchers..
            FileSystem system = from.getFileSystem();
            ArrayList<PathMatcher> includes = new ArrayList();
            ArrayList<PathMatcher> excludes = new ArrayList();
            ArrayList<PathMatcher> directories = new ArrayList();

            for (String pattern : patterns) {
                if (pattern.charAt(0) != '!') {
                    // include
                    includes.add(system.getPathMatcher("glob:".concat(pattern)));
                } else if (!pattern.endsWith("/**")) {
                    // exclude files
                    excludes.add(system.getPathMatcher("glob:".concat(pattern.substring(1))));
                } else {
                    // exclude directory
                    directories.add(system.getPathMatcher("glob:".concat(pattern.substring(1, pattern.length() - 3))));
                }
            }

            // Convert into Array
            this.includes = includes.toArray(new PathMatcher[includes.size()]);
            this.excludes = excludes.toArray(new PathMatcher[excludes.size()]);
            this.directories = directories.toArray(new PathMatcher[directories.size()]);

            // Walk file tree actually.
            Files.walkFileTree(from, EnumSet.noneOf(FileVisitOption.class), this.depth, this);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * @see java.nio.file.FileVisitor#preVisitDirectory(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
        // Retrieve relative path from base.
        Path relative = from.relativize(path);

        // Directory exclusion make fast traversing file tree.
        for (PathMatcher matcher : directories) {
            if (matcher.matches(relative)) {
                return SKIP_SUBTREE;
            }
        }

        switch (type) {
        case 0: // copy
        case 1: // move
            Files.createDirectories(to.resolve(relative));
            // fall-through to reduce footprint

        case 2: // delete
            return CONTINUE;

        case 4: // walk directory
            // skip root directory
            if (current != 1) add(path);

            // fall-through to reduce footprint

        case 3: // walk file
            // The {@link FileVisitor#visitFile(Object, BasicFileAttributes)} method is invoked for
            // all files, including directories, encountered at max depth. So we use the value which
            // is greater than the user specified max depth, and skip lowest files using
            // SKIP_SUBTREE value.
            //
            // At first, we must check max depth.
            if (current == depth) {
                return SKIP_SUBTREE;
            }

            // Then, we can count up current depth.
            current++;

            // API definition
            return CONTINUE;

        default:
            return current++ == 1 ? CONTINUE : visitor.preVisitDirectory(path, attrs);
        }
    }

    /**
     * @see java.nio.file.FileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
     */
    public FileVisitResult postVisitDirectory(Path path, IOException exc) throws IOException {
        switch (type) {
        case 0: // copy
            Files.setLastModifiedTime(to.resolve(from.relativize(path)), Files.getLastModifiedTime(path));
            return CONTINUE;

        case 1: // move
            Files.setLastModifiedTime(to.resolve(from.relativize(path)), Files.getLastModifiedTime(path));
            // fall-through to reduce footprint

        case 2: // delete
            Files.delete(path);
            // fall-through to reduce footprint

        case 3: // walk file
        case 4: // walk directory
            current--;
            return CONTINUE;

        default:
            return --current == 1 ? CONTINUE : visitor.postVisitDirectory(path, exc);
        }
    }

    /**
     * @see java.nio.file.FileVisitor#visitFile(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        // Retrieve relative path from base.
        Path relative = from.relativize(path);

        // File exclusion
        for (PathMatcher matcher : excludes) {
            if (matcher.matches(relative)) {
                return CONTINUE;
            }
        }

        // File inclusion
        for (PathMatcher matcher : includes) {
            if (matcher.matches(relative)) {
                return visit(path, attrs);
            }
        }
        return includes.length != 0 ? CONTINUE : visit(path, attrs);
    }

    /**
     * @see java.nio.file.FileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
     */
    public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
        return CONTINUE;
    }

    /**
     * <p>
     * Helper method to invoke file visit actions.
     * </p>
     * 
     * @param path
     * @param attrs
     * @return
     * @throws IOException
     */
    private FileVisitResult visit(Path path, BasicFileAttributes attrs) throws IOException {
        switch (type) {
        case 0: // copy
            Files.copy(path, to.resolve(from.relativize(path)), COPY_ATTRIBUTES, REPLACE_EXISTING);
            return CONTINUE;

        case 1: // move
            Files.move(path, to.resolve(from.relativize(path)), ATOMIC_MOVE, REPLACE_EXISTING);
            return CONTINUE;

        case 2: // delete
            Files.delete(path);
            return CONTINUE;

        case 3: // walk file
            add(path);
            // fall-through to reduce footprint

        case 4: // walk directory
            return CONTINUE;

        default:
            return visitor.visitFile(path, attrs);
        }
    }
}