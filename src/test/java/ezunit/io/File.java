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
package ezunit.io;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Iterator;

import ezbean.I;

/**
 * @version 2011/03/07 22:31:59
 */
public class File implements Path {

    /** The actual path to delegate. */
    private Path path;

    /**
     * <p>
     * Internal API
     * </p>
     * 
     * @param path
     */
    private File(Path path) {
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    public FileSystem getFileSystem() {
        return FileSystems.getDefault();
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAbsolute() {
        return path.isAbsolute();
    }

    /**
     * {@inheritDoc}
     */
    public File getRoot() {
        return new File(path.getRoot());
    }

    /**
     * {@inheritDoc}
     */
    public File getFileName() {
        return new File(path.getFileName());
    }

    /**
     * {@inheritDoc}
     */
    public File getParent() {
        return new File(path.getParent());
    }

    /**
     * {@inheritDoc}
     */
    public int getNameCount() {
        return path.getNameCount();
    }

    /**
     * {@inheritDoc}
     */
    public File getName(int index) {
        return new File(path.getName(index));
    }

    /**
     * {@inheritDoc}
     */
    public File subpath(int beginIndex, int endIndex) {
        return new File(path.subpath(beginIndex, endIndex));
    }

    /**
     * {@inheritDoc}
     */
    public boolean startsWith(Path other) {
        return path.startsWith(other);
    }

    /**
     * {@inheritDoc}
     */
    public boolean startsWith(String other) {
        return path.startsWith(other);
    }

    /**
     * {@inheritDoc}
     */
    public boolean endsWith(Path other) {
        return path.endsWith(other);
    }

    /**
     * {@inheritDoc}
     */
    public boolean endsWith(String other) {
        return path.endsWith(other);
    }

    /**
     * {@inheritDoc}
     */
    public File normalize() {
        return new File(path.normalize());
    }

    /**
     * {@inheritDoc}
     */
    public File resolve(Path other) {
        return new File(path.resolve(other));
    }

    /**
     * {@inheritDoc}
     */
    public File resolve(String other) {
        return new File(path.resolve(other));
    }

    /**
     * {@inheritDoc}
     */
    public File resolveSibling(Path other) {
        return new File(path.resolveSibling(other));
    }

    /**
     * {@inheritDoc}
     */
    public File resolveSibling(String other) {
        return new File(path.resolveSibling(other));
    }

    /**
     * {@inheritDoc}
     */
    public File relativize(Path other) {
        return new File(path.relativize(other));
    }

    /**
     * {@inheritDoc}
     */
    public URI toUri() {
        return path.toUri();
    }

    /**
     * {@inheritDoc}
     */
    public File toAbsolutePath() {
        return new File(path.toAbsolutePath());
    }

    /**
     * {@inheritDoc}
     */
    public File toRealPath(boolean resolveLinks) throws IOException {
        return new File(path.toRealPath(resolveLinks));
    }

    /**
     * {@inheritDoc}
     */
    public java.io.File toFile() {
        return path.toFile();
    }

    /**
     * {@inheritDoc}
     */
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        return path.register(watcher, events, modifiers);
    }

    /**
     * {@inheritDoc}
     */
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        return path.register(watcher, events);
    }

    /**
     * {@inheritDoc}
     */
    public Iterator<Path> iterator() {
        return path.iterator();
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(Path other) {
        return path.compareTo(other);
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other) {
        return path.equals(other);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return path.toString();
    }

    /**
     * <p>
     * Tests whether a file exists.
     * </p>
     * <p>
     * Note that the result of this method is immediately outdated. If this method indicates the
     * file exists then there is no guarantee that a subsequence access will succeed. Care should be
     * taken when using this method in security sensitive applications.
     * </p>
     * 
     * @return If the file exists; <code>false</code> if the file does not exist or its existence
     *         cannot be determined.
     */
    public boolean exists() {
        return Files.exists(path);
    }

    /**
     * <p>
     * Tests whether the file located by this path does not exist. This method is intended for cases
     * where it is required to take action when it can be confirmed that a file does not exist.
     * </p>
     * <p>
     * Note that this method is not the complement of the {@link #exists()} method. Where it is not
     * possible to determine if a file exists or not then both methods return <code>false</code>. As
     * with the {@link #exists()} method, the result of this method is immediately outdated. If this
     * method indicates the file does exist then there is no guarantee that a subsequence attempt to
     * create the file will succeed. Care should be taken when using this method in security
     * sensitive applications.
     * </p>
     * 
     * @return <code>true</code> if the file does not exist; <code>false</code> if the file exists
     *         or its existence cannot be determined.
     */
    public boolean notExists() {
        return Files.notExists(path);
    }

    /**
     * <p>
     * Tests whether a file is a directory.
     * </p>
     * 
     * @return <code>true</code> if the file is a directory; <code>false</code> if the file does not
     *         exist, is not a directory, or it cannot be determined if the file is directory or
     *         not.
     */
    public boolean isDirectory() {
        return Files.isDirectory(path);
    }

    /**
     * <p>
     * Tests whether a file is a regular file with opaque content.
     * </p>
     * 
     * @return <code>true</code> if the file is a regular file; <code>false</code> if the file does
     *         not exist, is not a direcregular filetory, or it cannot be determined if the file is
     *         regular file or not.
     */
    public boolean isFile() {
        return Files.isRegularFile(path);
    }

    /**
     * <p>
     * Creates a directory by creating all nonexistent parent directories first.
     * </p>
     */
    public void createDirectory() {
        try {
            BasicFileAttributes attribute = Files.getFileAttributeView(path, BasicFileAttributeView.class)
                    .readAttributes();

            if (!attribute.isDirectory()) {
                System.out.println(attribute.isRegularFile());
            }
        } catch (NoSuchFileException e) {
            try {
                Files.createDirectories(path);
            } catch (IOException e1) {
                throw I.quiet(e);
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Creates a new and empty file.
     * </p>
     */
    public void createFile() {
        try {
            Files.createFile(path);
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Locate the specified file path and return the plain {@link File} object.
     * </p>
     * 
     * @param filePath A location path.
     * @return A located {@link File}.
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     * @param filePath
     * @return
     */
    public static final File locate(String filePath) {
        return new File(Paths.get(filePath));
    }

    /**
     * <p>
     * Locate the specified file path and return the plain {@link File} object.
     * </p>
     * 
     * @param filePath A location path.
     * @param more A location path.
     * @return A located {@link File}.
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     * @param filePath
     * @return
     */
    public static final File locate(String filePath, String... more) {
        return new File(Paths.get(filePath, more));
    }

    /**
     * <p>
     * Locate the specified old file and return the plain {@link File} object.
     * </p>
     * 
     * @param filePath A location path.
     * @return A located {@link File}.
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static final File locate(java.io.File filePath) {
        return new File(filePath.toPath());
    }

    /**
     * <p>
     * Locate the specified file URL and return the plain {@link File} object.
     * </p>
     * 
     * @param filePath A location path.
     * @return A located {@link File}.
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static final File locate(URI filePath) {
        // Use File constructor with URI to resolve escaped character.
        return locate(new java.io.File(filePath));
    }

    /**
     * <p>
     * Locate the specified file URL and return the plain {@link File} object.
     * </p>
     * 
     * @param filePath A location path.
     * @return A located {@link File}.
     * @throws NullPointerException If the given file path is null.
     * @throws SecurityException If a security manager exists and its
     *             {@link SecurityManager#checkWrite(String)} method does not allow a file to be
     *             created.
     */
    public static final File locate(URL filePath) {
        try {
            return locate(filePath.toURI());
        } catch (URISyntaxException e) {
            return locate(filePath.getPath());
        }
    }
}
