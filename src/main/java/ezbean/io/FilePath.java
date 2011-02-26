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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import ezbean.Accessible;
import ezbean.I;
import ezbean.Listeners;

/**
 * Implement {@link Accessible} interface to delegate {@link ezbean.model.Model} load process to
 * {@link java.io.File}.
 * 
 * @version 2010/01/21 0:24:06
 */
@SuppressWarnings("serial")
public class FilePath extends java.io.File implements Accessible {

    /** The digest algorithm instance. */
    private static final MessageDigest digest;

    // initialization
    static {
        try {
            digest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            throw I.quiet(e);
        }
    }

    /** The affiliated archive for this file. */
    private final FilePath archive;

    /** The archiver for the affiliated archive. */
    private final Class<Archiver> archiver;

    /** The junction file for the archive. */
    private FilePath junction;

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
     * @param archive An affiliated archive of this file. The <code>null</code> is accepted.
     */
    FilePath(String path, FilePath archive) {
        super(path);

        this.archive = archive;
        this.archiver = FileSystem.findArchiver(getName());
    }

    /**
     * Retrieve junction point for this archive file.
     * 
     * @return A junction directory.
     */
    java.io.File getJunction() {
        // check cache
        if (junction == null) {
            // calculate digest
            synchronized (digest) {
                // Environment (not only OS but also Way of Execution) influences character equality
                // of file path (e.g. "c:/test.txt" in one environment, "C:/Test.txt" in other
                // environment). So we must normalize it.
                digest.update(getAbsolutePath().toLowerCase().getBytes());

                // dump byte array into hexadecimal digit sequence.
                StringBuilder builder = new StringBuilder();

                for (byte b : digest.digest()) {
                    builder.append(Integer.toHexString(b & 0xff));
                }

                // store junction directory
                junction = I.locate(FileSystem.temporaries, builder.toString());
            }
        }

        // API definition
        return junction;
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
     * @see java.io.File#createNewFile()
     */
    @Override
    public boolean createNewFile() throws IOException {
        return (archive == null) ? super.createNewFile() : false;
    }

    /**
     * @see java.io.File#delete()
     */
    @Override
    public boolean delete() {
        if (archive == null) {
            // delete a junction file if this file is archive and it has created a junction file
            // already
            FileSystem.delete(junction);

            // delete this file
            return super.delete();
        }
        return false;
    }

    /**
     * @see java.io.File#deleteOnExit()
     */
    @Override
    public void deleteOnExit() {
        if (archive == null) {
            super.deleteOnExit();
        }
    }

    /**
     * @see java.io.File#isDirectory()
     */
    @Override
    public boolean isDirectory() {
        return (archiver == null) ? super.isDirectory() : exists();
    }

    /**
     * @see java.io.File#getAbsoluteFile()
     */
    @Override
    public java.io.File getAbsoluteFile() {
        return I.locate(getAbsolutePath());
    }

    /**
     * @see java.io.File#getCanonicalFile()
     */
    @Override
    public java.io.File getCanonicalFile() throws IOException {
        return I.locate(getCanonicalPath());
    }

    /**
     * @see java.io.File#getParent()
     */
    @Override
    public String getParent() {
        String path = super.getParent();
        return (archive == null || !archive.getJunction().getPath().equals(path)) ? path : archive.getPath();
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
        return (archive != null && archive.getJunction().getPath().equals(path)) ? archive
                : new FilePath(path, archive);
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
     * @see java.io.File#list()
     */
    @Override
    public synchronized String[] list() {
        if (archiver == null) {
            return super.list();
        } else {
            // check diff
            long modified = lastModified();

            if (getJunction().lastModified() != modified) {
                FileSystem.delete(junction);

                try {
                    I.make(archiver).unpack(this, getJunction());

                    // you must set last modified date at the last
                    getJunction().setLastModified(modified);
                } catch (IOException e) {
                    return null; // API definition
                }
            }
            return getJunction().list();
        }
    }

    /**
     * @see java.io.File#listFiles()
     */
    @Override
    public java.io.File[] listFiles() {
        String[] names = list();

        if (names == null) {
            return null;
        }

        int size = names.length;
        java.io.File[] files = new java.io.File[size];

        for (int i = 0; i < size; i++) {
            if (archiver == null) {
                files[i] = new FilePath(getPath() + separator + names[i], archive);
            } else {
                files[i] = new FilePath(getJunction() + separator + names[i], this);
            }
        }
        return files;
    }

    /**
     * @see java.io.File#mkdir()
     */
    @Override
    public boolean mkdir() {
        return (archive == null) ? super.mkdir() : false;
    }

    /**
     * @see java.io.File#mkdirs()
     */
    @Override
    public boolean mkdirs() {
        return (archive == null) ? super.mkdirs() : false;
    }

    /**
     * @see java.io.File#renameTo(java.io.File)
     */
    @Override
    public boolean renameTo(java.io.File dest) {
        return (archive == null) ? super.renameTo(dest) : false;
    }

    /**
     * @see java.io.File#setLastModified(long)
     */
    @Override
    public boolean setLastModified(long time) {
        return (archive == null) ? super.setLastModified(time) : false;
    }

    /**
     * @see java.io.File#toURI()
     */
    @Override
    public URI toURI() {
        if (archiver == null) {
            return super.toURI();
        } else {
            String uri = super.toURI().toString();

            try {
                return new URI(uri.substring(0, uri.length() - 1));
            } catch (URISyntaxException e) {
                throw I.quiet(e);
            }
        }
    }

    /**
     * @see java.io.File#toURL()
     */
    @Override
    public URL toURL() throws MalformedURLException {
        return toURI().toURL();
    }

    /**
     * @see java.io.File#toString()
     */
    @Override
    public String toString() {
        String filePath = super.toString();

        if (archive != null) {
            filePath = archive + filePath.substring(archive.getJunction().getPath().length());
        }
        return filePath.replace(separatorChar, '/');
    }

    /**
     * @see ezbean.Accessible#access(int, java.lang.Object)
     */
    public Object access(int id, Object params) {
        return null; // do nothing
    }

    /**
     * @see ezbean.Accessible#context()
     */
    public Listeners context() {
        return null; // do nothing
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
            createDirectories(to.resolve(from.relativize(dir)));

            // API definition
            return CONTINUE;
        }

        /**
         * @see java.nio.file.SimpleFileVisitor#postVisitDirectory(java.lang.Object,
         *      java.io.IOException)
         */
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            setLastModifiedTime(to.resolve(from.relativize(dir)), getLastModifiedTime(dir));

            // API definition
            return CONTINUE;
        }

        /**
         * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object,
         *      java.nio.file.attribute.BasicFileAttributes)
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            copy(file, to.resolve(from.relativize(file)), REPLACE_EXISTING, COPY_ATTRIBUTES);

            // API definition
            return CONTINUE;
        }
    }
}
