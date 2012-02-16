/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package antibug;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

import kiss.I;

import org.junit.Rule;

/**
 * @version 2012/02/16 19:06:33
 */
public class OneTimeFile extends ReusableRule implements Path, CharSequence {

    /** The root directory for one time files. */
    private static final Path root = I.locateTemporary();

    /** The actual file system manager. */
    @Rule
    public static final CleanRoom clean = new CleanRoom(root);

    /** The counter for instances. */
    private static final AtomicInteger counter = new AtomicInteger();

    /** The actual temporary file. */
    private final Path path;

    /** The actual contents. */
    private final String contents;

    /**
     * <p>
     * Create temporary file with the specified contents.
     * </p>
     * 
     * @param contents A text contents.
     */
    public OneTimeFile(String contents) {
        String name = String.valueOf(counter.incrementAndGet());
        Path path = root.resolve(name);

        try {
            Files.write(path, contents.getBytes());
            System.out.println(Files.exists(path) + "   " + Files.readAllLines(path, I.$encoding));
        } catch (Exception e) {
            throw I.quiet(e);
        }

        this.contents = contents;
        this.path = clean.locateAbsent(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int length() {
        return contents.length();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char charAt(int index) {
        return contents.charAt(index);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        return contents.subSequence(start, end);
    }

    /**
     * {@inheritDoc}
     */
    public FileSystem getFileSystem() {
        return path.getFileSystem();
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
    public Path getRoot() {
        return path.getRoot();
    }

    /**
     * {@inheritDoc}
     */
    public Path getFileName() {
        return path.getFileName();
    }

    /**
     * {@inheritDoc}
     */
    public Path getParent() {
        return path.getParent();
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
    public Path getName(int index) {
        return path.getName(index);
    }

    /**
     * {@inheritDoc}
     */
    public Path subpath(int beginIndex, int endIndex) {
        return path.subpath(beginIndex, endIndex);
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
    public Path normalize() {
        return path.normalize();
    }

    /**
     * {@inheritDoc}
     */
    public Path resolve(Path other) {
        return path.resolve(other);
    }

    /**
     * {@inheritDoc}
     */
    public Path resolve(String other) {
        return path.resolve(other);
    }

    /**
     * {@inheritDoc}
     */
    public Path resolveSibling(Path other) {
        return path.resolveSibling(other);
    }

    /**
     * {@inheritDoc}
     */
    public Path resolveSibling(String other) {
        return path.resolveSibling(other);
    }

    /**
     * {@inheritDoc}
     */
    public Path relativize(Path other) {
        return path.relativize(other);
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
    public Path toAbsolutePath() {
        return path.toAbsolutePath();
    }

    /**
     * {@inheritDoc}
     */
    public Path toRealPath(LinkOption... options) throws IOException {
        return path.toRealPath(options);
    }

    /**
     * {@inheritDoc}
     */
    public File toFile() {
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
    @Override
    public String toString() {
        return contents;
    }
}
