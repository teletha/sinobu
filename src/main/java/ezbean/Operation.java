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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @version 2011/03/10 23:51:35
 */
class Operation extends SimpleFileVisitor<Path> {

    /** The source location. */
    private final Path from;

    /** The target location. */
    private final Path to;

    /** 0:copy 1:move 2:delete. */
    private final int type;

    /**
     * @param from
     * @param to
     */
    Operation(Path from, Path to, int type) {
        this.from = from.getParent();
        this.to = to;
        this.type = type;
    }

    /**
     * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        if (type != 2) {
            Files.createDirectories(to.resolve(from.relativize(dir)));
        }
        return CONTINUE;
    }

    /**
     * @see java.nio.file.SimpleFileVisitor#postVisitDirectory(java.lang.Object,
     *      java.io.IOException)
     */
    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        switch (type) {
        case 0:
            Files.setLastModifiedTime(to.resolve(from.relativize(dir)), Files.getLastModifiedTime(dir));
            break;

        case 1:
            Files.setLastModifiedTime(to.resolve(from.relativize(dir)), Files.getLastModifiedTime(dir));
            // pass-through

        case 2:
            Files.delete(dir);
            break;
        }
        return CONTINUE;
    }

    /**
     * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        switch (type) {
        case 0:
            Files.copy(file, to.resolve(from.relativize(file)), COPY_ATTRIBUTES, REPLACE_EXISTING);
            break;

        case 1:
            Files.move(file, to.resolve(from.relativize(file)), REPLACE_EXISTING);
            break;

        case 2:
            Files.delete(file);
            break;
        }
        return CONTINUE;
    }
}