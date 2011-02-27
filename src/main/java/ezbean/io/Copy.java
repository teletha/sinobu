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
import static java.nio.file.Files.*;
import static java.nio.file.StandardCopyOption.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @version 2011/02/27 20:34:11
 */
class Copy extends SimpleFileVisitor<Path> {

    /** The target location. */
    private final Path to;

    /** The start position. */
    private final int position;

    /** The operation mode. */
    private final boolean copy = true;

    /** The sccess flag. */
    boolean success = true;

    /**
     * @param from
     * @param to
     */
    Copy(Path from, Path to) {
        this.to = to;
        this.position = from.getNameCount() - 1;
    }

    /**
     * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
        createDirectories(to.resolve(path.subpath(position, path.getNameCount())));

        // API definition
        return CONTINUE;
    }

    /**
     * @see java.nio.file.SimpleFileVisitor#postVisitDirectory(java.lang.Object,
     *      java.io.IOException)
     */
    @Override
    public FileVisitResult postVisitDirectory(Path path, IOException exc) throws IOException {
        setLastModifiedTime(to.resolve(path.subpath(position, path.getNameCount())), getLastModifiedTime(path));

        // API definition
        return CONTINUE;
    }

    /**
     * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        copy(path, to.resolve(path.subpath(position, path.getNameCount())), REPLACE_EXISTING, COPY_ATTRIBUTES);

        // API definition
        return CONTINUE;
    }
}
