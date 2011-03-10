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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @version 2011/03/10 17:54:08
 */
class Paths implements FileVisitor<Path> {

    FileVisitor<Path> visitor;

    Path base;

    PathMatcher[] includes;

    PathMatcher[] excludes;

    PathMatcher[] excludeDirectories;

    /**
     * @param visitor
     * @param base
     * @param patterns
     */
    Paths(Path base, FileVisitor<Path> visitor, String... patterns) {
        this.visitor = visitor;
        this.base = base;

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
        return visitor.visitFile(file, attrs);
    }

    /**
     * @see java.nio.file.FileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
     */
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return visitor.visitFileFailed(file, exc);
    }
}
