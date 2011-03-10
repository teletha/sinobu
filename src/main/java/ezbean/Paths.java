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

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

/**
 * @version 2011/03/10 17:54:08
 */
class Paths implements FileVisitor<Path> {

    FileVisitor<Path> visitor;

    Path base;

    PathMatcher[] includes;

    PathMatcher[] excludes;

    PathMatcher[] excludeDirectories;

    /** The prefix size of root path. */
    private final int start;

    /**
     * @param visitor
     * @param base
     * @param patterns
     */
    Paths(Path base, FileVisitor<Path> visitor, String... patterns) {
        this.visitor = visitor;
        this.base = base;
        this.start = base.getNameCount();

        FileSystem system = base.getFileSystem();
        ArrayList<PathMatcher> includes = new ArrayList();
        ArrayList<PathMatcher> excludes = new ArrayList();
        ArrayList<PathMatcher> excludeDirectories = new ArrayList();

        for (String pattern : patterns) {
            if (pattern.charAt(0) == '!') {
                // exclude
                if (pattern.endsWith("/**")) {
                    // directory match
                    System.out.println("glob:".concat(pattern.substring(1, pattern.length() - 3)));
                    excludeDirectories.add(system.getPathMatcher("glob:".concat(pattern.substring(1, pattern.length() - 3))));
                } else {
                    // anything match
                    excludes.add(system.getPathMatcher("glob:".concat(pattern.substring(1))));
                }
            } else {
                // include
                includes.add(system.getPathMatcher("glob:".concat(pattern)));
            }
        }

        // Convert into Array
        this.includes = includes.toArray(new PathMatcher[includes.size()]);
        this.excludes = excludes.toArray(new PathMatcher[excludes.size()]);
        this.excludeDirectories = excludeDirectories.toArray(new PathMatcher[excludeDirectories.size()]);

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
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
        int end = path.getNameCount();

        if (start != end) {
            Path relative = path.subpath(start, end);

            for (PathMatcher matcher : excludeDirectories) {
                if (matcher.matches(relative)) {
                    return SKIP_SUBTREE;
                }
            }
        }
        return visitor.preVisitDirectory(path, attrs);
    }

    /**
     * @see java.nio.file.FileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
     */
    public FileVisitResult postVisitDirectory(Path path, IOException exc) throws IOException {
        return visitor.postVisitDirectory(path, exc);
    }

    /**
     * @see java.nio.file.FileVisitor#visitFile(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        Path relative = path.subpath(start, path.getNameCount());

        for (PathMatcher matcher : excludes) {
            if (matcher.matches(relative)) {
                return CONTINUE;
            }
        }

        if (includes.length == 0) {
            return visitor.visitFile(path, attrs);
        } else {
            for (PathMatcher matcher : includes) {
                if (matcher.matches(relative)) {
                    return visitor.visitFile(path, attrs);
                }
            }
        }
        return CONTINUE;
    }

    /**
     * @see java.nio.file.FileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
     */
    public FileVisitResult visitFileFailed(Path path, IOException exc) throws IOException {
        return visitor.visitFileFailed(path, exc);
    }
}
