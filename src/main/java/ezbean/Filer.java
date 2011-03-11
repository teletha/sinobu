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
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 * @version 2011/03/11 8:27:35
 */
class Filer extends SimpleFileVisitor<Path> {

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

    /** The source location. */
    private final Path from;

    /** The target location. */
    private final Path to;

    /** 0:copy 1:move 2:delete. */
    private final int type;

    /**
     * @param visitor
     * @param base
     * @param patterns
     */
    public Filer(Path base, Path to, int type, FileVisitor<Path> visitor, String... patterns) {
        this.from = base.getParent();
        this.to = to;
        this.type = type;
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
        switch (type) {
        case 0:
        case 1:
            Files.createDirectories(to.resolve(from.relativize(path)));
            return CONTINUE;

        case 2:
            return CONTINUE;

        default:
            return visitor.preVisitDirectory(path, attrs);
        }
    }

    /**
     * @see java.nio.file.FileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
     */
    @Override
    public FileVisitResult postVisitDirectory(Path path, IOException exc) throws IOException {
        switch (type) {
        case 0:
            Files.setLastModifiedTime(to.resolve(from.relativize(path)), Files.getLastModifiedTime(path));
            return CONTINUE;

        case 1:
            Files.setLastModifiedTime(to.resolve(from.relativize(path)), Files.getLastModifiedTime(path));
            // pass-through

        case 2:
            Files.delete(path);
            return CONTINUE;

        default:
            return visitor.postVisitDirectory(path, exc);
        }

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
        switch (type) {
        case 0:
            Files.copy(path, to.resolve(from.relativize(path)), COPY_ATTRIBUTES, REPLACE_EXISTING);
            return CONTINUE;

        case 1:
            Files.move(path, to.resolve(from.relativize(path)), ATOMIC_MOVE, REPLACE_EXISTING);
            return CONTINUE;

        case 2:
            Files.delete(path);
            return CONTINUE;

        default:
            return visitor.visitFile(path, attrs);
        }
    }
}
