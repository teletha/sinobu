/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package antibug.file;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.FileSystem;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.WatchEvent.Kind;
import java.nio.file.WatchEvent.Modifier;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Collections;
import java.util.Iterator;

import kiss.I;

/**
 * @version 2012/02/17 15:42:15
 */
public class Memo implements CharSequence, Path {

    /** The actual contents. */
    StringBuilder contents;

    /**
     * 
     */
    public Memo() {
        this(null);
    }

    /**
     * @param contents
     */
    public Memo(String contents) {
        if (contents == null) {
            contents = "";
        }
        this.contents = new StringBuilder(contents);
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
    @Override
    public int compareTo(Path other) {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean endsWith(Path other) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean endsWith(String other) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getFileName() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileSystem getFileSystem() {
        return I.make(MemoFS.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getName(int index) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNameCount() {
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getParent() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path getRoot() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAbsolute() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Iterator<Path> iterator() {
        return Collections.singletonList((Path) this).iterator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path normalize() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WatchKey register(WatchService watcher, Kind<?>... events) throws IOException {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public WatchKey register(WatchService watcher, Kind<?>[] events, Modifier... modifiers) throws IOException {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path relativize(Path other) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolve(Path other) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolve(String other) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveSibling(Path other) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path resolveSibling(String other) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startsWith(Path other) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean startsWith(String other) {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path toAbsolutePath() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public File toFile() {
        // If this exception will be thrown, it is bug of this program. So we must rethrow the
        // wrapped error in here.
        throw new Error();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public URI toUri() {
        try {
            return new URI("memo:" + URLEncoder.encode(contents.toString(), "UTF-8"));
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return contents.toString();
    }
}
