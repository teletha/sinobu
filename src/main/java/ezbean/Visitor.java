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
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;

/**
 * @version 2011/11/17 15:23:56
 */
@SuppressWarnings("serial")
class Visitor extends ArrayList<Path> implements FileVisitor<Path> {

    /** The source. */
    Path from;

    /** The destination. */
    Path to;

    /** The operation type. */
    private final int type;

    /** The actual {@link FileVisitor} to delegate. */
    private final FileVisitor<Path> visitor;

    /** The include file patterns. */
    final PathMatcher[] includes;

    /** The exclude file patterns. */
    final PathMatcher[] excludes;

    /** The exclude directory pattern. */
    final PathMatcher[] directories;

    /** We must skip root directory? */
    private boolean root = false;

    /**
     * <p>
     * Utility for file tree traversal.
     * </p>
     */
    Visitor(Path from, Path to, int type, FileVisitor visitor, String... patterns) {
        this.type = type;
        this.visitor = visitor;

        try {
            boolean directory = Files.isDirectory(from);

            // The copy and move operations need the root path.
            this.from = directory && type < 2 ? from.getParent() : from;

            // The copy and move operations need destination. If the source is file, so destination
            // must be file and its name is equal to source file.
            this.to = !directory && type < 2 && Files.isDirectory(to) ? to.resolve(from.getFileName()) : to;

            if (type < 2) {
                Files.createDirectories(to.getParent());
            }

            // Parse and create path matchers.

            // Default file system doesn't support close method, so we can ignore to release
            // resource.
            @SuppressWarnings("resource")
            FileSystem system = from.getFileSystem();
            ArrayList<PathMatcher> includes = new ArrayList();
            ArrayList<PathMatcher> excludes = new ArrayList();
            ArrayList<PathMatcher> directories = new ArrayList();

            for (String pattern : patterns) {
                // convert pattern to reduce unnecessary file system scanning
                if (pattern.equals("*")) {
                    pattern = "!*/**";
                } else if (pattern.equals("**")) {
                    this.from = from;
                    this.root = true;
                    continue;
                }

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
            if (type <= 5) Files.walkFileTree(from, Collections.EMPTY_SET, Integer.MAX_VALUE, this);
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

        // Skip root directory.
        // Directory exclusion make fast traversing file tree.
        for (PathMatcher matcher : directories) {
            // Normally, we can't use identical equal against path object. But only root path object
            // is passed as parameter value, so we can use identical equal here.
            if (from != path && matcher.matches(relative)) {
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
            add(path);
            // fall-through to reduce footprint

        case 3: // walk file
        case 6: // observe dirctory
            return CONTINUE;

        default:
            // Skip root directory
            return from == path ? CONTINUE : visitor.preVisitDirectory(path, attrs);
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
            if (!root || from != path) {
                Files.delete(path);
            }
            // fall-through to reduce footprint

        case 3: // walk file
        case 4: // walk directory
            return CONTINUE;

        default:
            // Skip root directory.
            return from == path ? CONTINUE : visitor.postVisitDirectory(path, exc);
        }
    }

    /**
     * @see java.nio.file.FileVisitor#visitFile(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        // Retrieve relative path from base.
        Path relative = from.relativize(path);

        if (accept(relative)) {
            switch (type) {
            case 0: // copy
                Path dest = to.resolve(relative);

                if (Files.notExists(dest) || !Files.getLastModifiedTime(dest).equals(attrs.lastModifiedTime())) {
                    Files.copy(path, dest, COPY_ATTRIBUTES, REPLACE_EXISTING);
                }
                return CONTINUE;

            case 1: // move
                dest = to.resolve(relative);

                if (Files.notExists(dest) || !Files.getLastModifiedTime(dest).equals(attrs.lastModifiedTime())) {
                    Files.move(path, dest, ATOMIC_MOVE, REPLACE_EXISTING);
                    return CONTINUE;
                }

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
        } else {
            return CONTINUE;
        }
    }

    /**
     * @see java.nio.file.FileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
     */
    public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
        return CONTINUE;
    }

    /**
     * <p>
     * Helper method to test whether the path is acceptable or not.
     * </p>
     * 
     * @param path A target path.
     * @return A result.
     */
    boolean accept(Path path) {
        // File exclusion
        for (PathMatcher matcher : excludes) {
            if (matcher.matches(path)) {
                return false;
            }
        }

        // File inclusion
        for (PathMatcher matcher : includes) {
            if (matcher.matches(path)) {
                return true;
            }
        }
        return includes.length == 0;
    }
}