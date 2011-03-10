/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import ezbean.I;
import ezbean.PathSet;

/**
 * @version 2011/02/27 19:03:08
 */
@SuppressWarnings("serial")
public class FilePath extends java.io.File {

    /** The actual filter set for directory only matcher. */
    private Set<Wildcard> includeFile = new CopyOnWriteArraySet();

    /** The actual filter set for directory only matcher. */
    private Set<Wildcard> includeDirectory = new CopyOnWriteArraySet();

    /** The actual filter set for directory only matcher. */
    private Set<Wildcard> excludeFile = new CopyOnWriteArraySet();

    /** The actual filter set for directory only matcher. */
    private Set<Wildcard> excludeDirectory = new CopyOnWriteArraySet();

    /**
     * Create File instance.
     * 
     * @param path A path to this file. The <code>null</code> is not accepted.
     */
    public FilePath(String path) {
        super(path);
    }

    /**
     * Create File instance.
     * 
     * @param path A path to this file. The <code>null</code> is not accepted.
     */
    public FilePath(String parent, String child) {
        super(parent, child);
    }

    /**
     * @param parent
     * @param child
     */
    public FilePath(File parent, String child) {
        super(parent, child);
    }

    /**
     * <p>
     * Reads a basic attributes associated with a file in a file system as a bulk operation.
     * </p>
     * 
     * @return
     */
    public BasicFileAttributes attributes() {
        try {
            return readAttributes(toPath(), BasicFileAttributes.class);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * <p>
     * Delete the target file or directory. If the target is file, it's file will be deleted. If the
     * target is directory, all files, all directories and itself will be recursively deleted. The
     * directory is no need to be empty in order to be deleted.
     * </p>
     * 
     * @return <code>true</code> if and only if the file or directory is successfully deleted,
     *         <code>false</code> otherwise.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkDelete(String)} method denies delete access to the
     *             target file and its contents.
     */
    @Override
    public boolean delete() {
        if (isFile()) {
            return super.delete();
        } else {
            Delete operation = new Delete();

            scan(operation);

            return operation.success;
        }
    }

    /**
     * @version 2011/02/16 11:55:42
     */
    private static final class Delete extends SimpleFileVisitor<Path> {

        /** The sccess flag. */
        private boolean success = true;

        /**
         * @see java.nio.file.SimpleFileVisitor#postVisitDirectory(java.lang.Object,
         *      java.io.IOException)
         */
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            try {
                Files.deleteIfExists(dir);
            } catch (Exception e) {
                success = false;
            }
            return CONTINUE;
        }

        /**
         * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object,
         *      java.nio.file.attribute.BasicFileAttributes)
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            try {
                Files.deleteIfExists(file);
            } catch (Exception e) {
                success = false;
            }
            return CONTINUE;
        }
    }

    /**
     * @see java.io.File#getAbsoluteFile()
     */
    @Override
    public FilePath getAbsoluteFile() {
        return I.locate(getAbsolutePath());
    }

    /**
     * @see java.io.File#getCanonicalFile()
     */
    @Override
    public FilePath getCanonicalFile() throws IOException {
        return I.locate(getCanonicalPath());
    }

    /**
     * @see java.io.File#getParentFile()
     */
    @Override
    public FilePath getParentFile() {
        String path = super.getParent();

        if (path == null) {
            return null;
        }
        return new FilePath(path);
    }

    /**
     * @see java.io.File#lastModified()
     */
    @Override
    public long lastModified() {
        BasicFileAttributes attributes = attributes();
        return attributes == null ? -1 : attributes.lastModifiedTime().toMillis();
    }

    /**
     * @see java.io.File#listFiles()
     */
    @Override
    public FilePath[] listFiles() {
        String[] names = list();

        if (names == null) {
            return null;
        }

        int size = names.length;
        FilePath[] files = new FilePath[size];

        for (int i = 0; i < size; i++) {
            files[i] = new FilePath(getPath() + separator + names[i]);
        }
        return files;
    }

    /**
     * @see java.io.File#toString()
     */
    @Override
    public String toString() {
        return super.toString().replace(separatorChar, '/');
    }

    /**
     * <p>
     * Specify the file name patterns that you want to include.
     * </p>
     * 
     * @param patterns A include file patterns.
     * @return {@link FilePath} instance to chain API.
     */
    public FilePath include(String... patterns) {
        return parse(patterns, includeFile, includeDirectory);
    }

    /**
     * <p>
     * Specify the directory name patterns that you want to exclude.
     * </p>
     * 
     * @param patterns A exclude directory patterns.
     * @return {@link FilePath} instance to chain API.
     */
    public FilePath exclude(String... patterns) {
        return parse(patterns, excludeFile, excludeDirectory);
    }

    /**
     * <p>
     * Exclude the default
     * </p>
     * 
     * @return {@link FilePath} instance to chain API.
     */
    public FilePath excludeDefaults() {
        return exclude("**/.*", ".*/**", "CVS/**", "SCCS/**");
    }

    /**
     * <p>
     * Helper method to parse the specified patterns and store it for the suitable collection.
     * </p>
     */
    private FilePath parse(String[] patterns, Set<Wildcard> forFile, Set<Wildcard> forDirectory) {
        for (String pattern : patterns) {
            String[] parsed = pattern.replace(separatorChar, '/')
                    .replaceAll("\\*{3,}", "**")
                    .replaceAll("\\*\\*([^/])", "**/*$1")
                    .replaceAll("([^/])\\*\\*", "$1*/**")
                    .replace("/$", "/**")
                    .replace("^/", "")
                    .replaceAll("\\*\\*/\\*\\*", "**")
                    .split("/");

            if (parsed.length != 2) {
                throw new IllegalArgumentException("The pattern '" + pattern + "' is too complicated. PathSet accepts the following patterns only:\r\n  **/file.name\r\n  dir name/**");
            } else {
                if (parsed[0].equals("**")) {
                    forFile.add(new Wildcard(parsed[1]));
                } else if (parsed[1].equals("**")) {
                    forDirectory.add(new Wildcard(parsed[0]));
                }
            }
        }

        // API chain
        return this;
    }

    /**
     * <p>
     * Reset the current settings except for base directory.
     * </p>
     * 
     * @return {@link PathSet} instance to chain API.
     */
    public FilePath reset() {
        includeFile.clear();
        excludeFile.clear();
        includeDirectory.clear();
        excludeDirectory.clear();

        // API chain
        return this;
    }

    /**
     * <p>
     * Scan the file system from the specified base directory and its children. If you specify the
     * include/exclude patterns, this method recognize it.
     * </p>
     * 
     * @param vistor A file visitor that all accepted files and directories are passed.
     */
    public void scan(FileVisitor<Path> vistor) {
        try {
            walkFileTree(toPath(), EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new Traveler(vistor, excludeDirectory, excludeFile, includeFile, includeDirectory));
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * Generic method to copy a input {@link File} to an output {@link File}.
     * 
     * @param output An outout {@link File} object which can be file or directory.
     * @throws NullPointerException If the specified input or output file is <code>null</code>.
     * @throws IOException If an I/O error occurs.
     * @throws FileNotFoundException If the specified input file is not found. If the input file is
     *             directory and the output file is <em>not</em> directory.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public boolean copyTo(File output) {
        if (isDirectory()) {
            if (output.isFile()) {
                throw I.quiet(new FileNotFoundException());
            }
            Copy operation = new Copy(toPath(), output.toPath());

            scan(operation);

            return operation.success;
        } else {
            // If the input is file, output can accept file or directory.
            try {
                Path out = output.toPath();

                if (output.isDirectory()) {
                    out = out.resolve(getName());
                }

                // assure that the parent directories exist
                createDirectories(out.getParent());

                // copy data actually
                copy(toPath(), out, REPLACE_EXISTING, COPY_ATTRIBUTES);

                // API definition
                return true;
            } catch (IOException e) {
                // API definition
                return false;
            }
        }
    }

}
