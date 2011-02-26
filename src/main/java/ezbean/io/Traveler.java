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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;

/**
 * @version 2011/02/27 8:17:02
 */
class Traveler implements FileVisitor<Path> {

    /** The flag for includ patterns for files and directories. */
    private final boolean hasInclude;

    /** The simple file include patterns. */
    private final Wildcard[] includeFile;

    /** We should exclude something? */
    private final int includeFileSize;

    /** The simple directory include patterns. */
    private final Wildcard[] includeDirectory;

    /** We should exclude something? */
    private final int includeDirectorySize;

    /** The simple file exclude patterns. */
    private final Wildcard[] excludeFile;

    /** We should exclude something? */
    private final int excludeFileSize;

    /** The simple directory exclude patterns. */
    private final Wildcard[] excludeDirectory;

    /** We should exclude something? */
    private final int excludeDirectorySize;

    /** The actual file visitor. */
    private final FileVisitor<Path> delegator;

    /** The current depth of file system. */
    private int depth = 0;

    /** The last depth that we should include all files. */
    private int depthForDirectoryIncluding = Integer.MAX_VALUE;

    /**
     * @param delegator
     */
    Traveler(FileVisitor<Path> delegator, Set<Wildcard> excludeDirectory, Set<Wildcard> excludeFile, Set<Wildcard> includeFile, Set<Wildcard> includeDirectory) {
        this.delegator = delegator;
        this.includeFile = includeFile.toArray(new Wildcard[includeFile.size()]);
        this.excludeFile = excludeFile.toArray(new Wildcard[excludeFile.size()]);
        this.includeDirectory = includeDirectory.toArray(new Wildcard[includeDirectory.size()]);
        this.excludeDirectory = excludeDirectory.toArray(new Wildcard[excludeDirectory.size()]);

        includeFileSize = this.includeFile.length;
        excludeFileSize = this.excludeFile.length;
        includeDirectorySize = this.includeDirectory.length;
        excludeDirectorySize = this.excludeDirectory.length;
        hasInclude = includeFileSize + includeDirectorySize != 0;
    }

    /**
     * @see java.nio.file.FileVisitor#visitFile(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        String name = file.getFileName().toString();

        // Check excluding because of excluding pattern has high priority.
        for (int i = 0; i < excludeFileSize; i++) {
            if (excludeFile[i].match(name)) {
                return CONTINUE; // Discard the current file.
            }
        }

        if (!hasInclude) {
            // We must pick up all files.
            return delegator.visitFile(file, attrs);
        } else {
            // We must pick up the specified files and the others are discarded.

            // Chech whether the current directory is unconditional including or not.
            if (depthForDirectoryIncluding <= depth) {
                return delegator.visitFile(file, attrs);
            }

            // Check file including because some files are discarded.
            for (int i = 0; i < includeFileSize; i++) {
                if (includeFile[i].match(name)) {
                    return delegator.visitFile(file, attrs);
                }
            }

            // Any pattern doesn't match the curretn file, so we must discard it.
            return CONTINUE;
        }
    }

    /**
     * @see java.nio.file.FileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
     */
    public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        return CONTINUE;
    }

    /**
     * @see java.nio.file.FileVisitor#preVisitDirectory(java.lang.Object,
     *      java.nio.file.attribute.BasicFileAttributes)
     */
    public FileVisitResult preVisitDirectory(Path directory, BasicFileAttributes attrs) throws IOException {
        depth++;

        // Root directory is unconditionaly included.
        if (depth == 1) return delegator.preVisitDirectory(directory, attrs);

        // Cache directory name for reuse.
        String name = directory.getFileName().toString();

        // Check excluding because of excluding pattern has high priority.
        for (int i = 0; i < excludeDirectorySize; i++) {
            if (excludeDirectory[i].match(name)) {
                return SKIP_SUBTREE; // Discard the current directory.
            }
        }

        // Check directory including to reduce the evaluation on files.
        for (int i = 0; i < includeDirectorySize; i++) {
            if (includeDirectory[i].match(name)) {
                depthForDirectoryIncluding = depth; // record depth

                break;
            }
        }

        // delegation
        return delegator.preVisitDirectory(directory, attrs);
    }

    /**
     * @see java.nio.file.FileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
     */
    public FileVisitResult postVisitDirectory(Path directory, IOException exc) throws IOException {
        if (depth == depthForDirectoryIncluding) {
            depthForDirectoryIncluding = Integer.MAX_VALUE; // reset
        }

        depth--;

        // delegation
        return delegator.postVisitDirectory(directory, exc);
    }
}
