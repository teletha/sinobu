/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import static java.nio.file.FileVisitResult.*;
import static java.nio.file.StandardCopyOption.*;
import static java.nio.file.StandardWatchEventKinds.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystem;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.function.BiPredicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.sun.nio.zipfs.ZipPath;

/**
 * @version 2014/01/14 9:37:54
 */
@SuppressWarnings({"serial", "unchecked"})
class Visitor extends ArrayList<Path>implements FileVisitor<Path>, Runnable, Disposable {

    // =======================================================
    // For Pattern Matching Facility
    // =======================================================

    private Path original;

    /** The source. */
    private Path from;

    /** The destination. */
    private Path to;

    /** The operation type. */
    private int type;

    /** The actual {@link FileVisitor} to delegate. */
    private FileVisitor<Path> visitor;

    /** The include file patterns. */
    private BiPredicate<Path, BasicFileAttributes> includes;

    /** The exclude file patterns. */
    private PathMatcher[] excludes;

    /** The exclude directory pattern. */
    private PathMatcher[] directories;

    /** Can we accept root directory? */
    private boolean root = true;

    /** Flags whether the current directory can be deleted or not. */
    private Deque<Boolean> deletable;

    /** The zip archiver. */
    private ZipOutputStream zip;

    /**
     * <p>
     * Utility for file tree traversal.
     * </p>
     * <p>
     * Type parameter represents the following:
     * </p>
     * <ol>
     * <li>0 - copy</li>
     * <li>1 - move</li>
     * <li>2 - delete</li>
     * <li>3 - file scan</li>
     * <li>4 - file and directory with {@link FileVisitor}</li>
     * <li>5 - directory scan</li>
     * <li>6 - observe</li>
     * <li>-1 - zip</li>
     * </ol>
     */
    Visitor(Path from, Path to, int type, FileVisitor visitor, String... patterns) {
        this(from, to, type, visitor, (BiPredicate) null);

        if (patterns == null) {
            patterns = new String[0];
        }

        // Parse and create path matchers.
        //
        // Default file system doesn't support close method, so we can ignore to release resource.
        FileSystem system = from.getFileSystem();
        ArrayList<PathMatcher> excludes = new ArrayList();
        ArrayList<PathMatcher> directories = new ArrayList();

        for (String pattern : patterns) {
            // convert pattern to reduce unnecessary file system scanning
            if (pattern.equals("*")) {
                if (type < 5) {
                    pattern = "!*/**";
                } else {
                    this.from = from;
                    this.root = false;
                }
            } else if (pattern.equals("**")) {
                this.from = from;
                this.root = false;
                continue;
            }

            if (pattern.charAt(0) != '!') {
                // include
                PathMatcher matcher = system.getPathMatcher("glob:".concat(pattern));
                includes = includes.and((path, attrs) -> matcher.matches(path));
            } else if (!pattern.endsWith("/**")) {
                // exclude files
                if (type < 5) {
                    excludes.add(system.getPathMatcher("glob:".concat(pattern.substring(1))));
                } else {
                    directories.add(system.getPathMatcher("glob:".concat(pattern.substring(1))));
                }
            } else {
                // exclude directory
                directories.add(system.getPathMatcher("glob:".concat(pattern.substring(1, pattern.length() - 3)
                        .concat(from instanceof ZipPath ? "/" : ""))));
            }
        }

        // Convert into Array
        this.excludes = excludes.toArray(new PathMatcher[excludes.size()]);
        this.directories = directories.toArray(new PathMatcher[directories.size()]);
    }

    /**
     * <p>
     * Utility for file tree traversal.
     * </p>
     * <p>
     * Type parameter represents the following:
     * </p>
     * <ol>
     * <li>0 - copy</li>
     * <li>1 - move</li>
     * <li>2 - delete</li>
     * <li>3 - file scan</li>
     * <li>4 - file and directory with {@link FileVisitor}</li>
     * <li>5 - directory scan</li>
     * <li>6 - observe</li>
     * <li>-1 - zip</li>
     * </ol>
     */
    Visitor(Path from, Path to, int type, FileVisitor visitor, BiPredicate<Path, BasicFileAttributes> filter) {
        this.original = from;
        this.type = type;
        this.visitor = visitor;

        try {
            boolean directory = Files.isDirectory(from);

            // The copy and move operations need the root path.
            this.from = directory && type < 2 ? from.getParent() : from;

            // The copy and move operations need destination. If the source is file, so destination
            // must be file and its name is equal to source file.
            this.to = !directory && type < 2 && Files.isDirectory(to) ? to.resolve(from.getFileName()) : to;

            if (type < 2 && 1 < to.getNameCount()) {
                Files.createDirectories(to.getParent());
            }

            if (filter == null) {
                filter = (path, attrs) -> true;
            } else {
                root = false;
            }

            // Convert into Array
            this.includes = filter;
            this.excludes = new PathMatcher[0];
            this.directories = new PathMatcher[0];

            if (type == -1) this.zip = new ZipOutputStream(Files.newOutputStream(to), I.$encoding);

            if (type == 2 || type == 3) {
                deletable = new ArrayDeque();
            }
        } catch (IOException e) {
            throw I.quiet(e);
        }
    }

    /**
     * <p>
     * Walk file tree actually.
     * </p>
     * 
     * @return
     */
    Visitor walk() {
        try {
            Files.walkFileTree(original, Collections.EMPTY_SET, Integer.MAX_VALUE, this);
        } catch (IOException e) {
            throw I.quiet(e);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attrs) throws IOException {
        // Retrieve relative path from base.
        Path relative = from.relativize(path);
        // Skip root directory.
        // Directory exclusion make fast traversing file tree.
        for (PathMatcher matcher : directories) {
            // Normally, we can't use identical equal against path object. But only root path object
            // is passed as parameter value, so we can use identical equal here.
            if (from != path && matcher.matches(relative)) {
                return SKIP_SUBTREE;
            }
        }

        switch (type) {
        case 0: // copy
        case 1: // move
            Files.createDirectories(to.resolve(relative));
            // fall-through to reduce footprint

        case 2: // delete
        case 3: // walk file
        case -1: // zip
            return CONTINUE;

        case 5: // walk directory
            if ((root || from != path) && accept(relative, attrs)) add(path);
            // fall-through to reduce footprint

        case 6: // observe dirctory
            return CONTINUE;

        default: // walk file and directory with visitor
            // Skip root directory
            return from == path ? CONTINUE : visitor.preVisitDirectory(path, attrs);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileVisitResult postVisitDirectory(Path path, IOException e) throws IOException {
        switch (type) {
        case 0: // copy
        case 1: // move
            Files.setLastModifiedTime(to.resolve(from.relativize(path)), Files.getLastModifiedTime(path));
            if (type == 0 || Files.list(path).iterator().hasNext()) return CONTINUE;
            // fall-through to reduce footprint

        case 2: // delete
            if (root || from != path) {
                Files.delete(path);
            }
            // fall-through to reduce footprint

        case 3: // walk file
        case 5: // walk directory
        case -1: // zip
            return CONTINUE;

        default: // walk file and directory with visitor
            // Skip root directory.
            return from == path ? CONTINUE : visitor.postVisitDirectory(path, e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
        if (type < 5) {
            // Retrieve relative path from base.
            Path relative = from.relativize(path);

            if (accept(relative, attrs)) {
                switch (type) {
                case 0: // copy
                    Files.copy(path, to.resolve(relative), COPY_ATTRIBUTES, REPLACE_EXISTING);
                    break;

                case 1: // move
                    Files.move(path, to.resolve(relative), REPLACE_EXISTING);
                    break;

                case 2: // delete
                    Files.delete(path);
                    break;

                case -1: // zip
                    ZipEntry entry = new ZipEntry(relative.toString().replace(File.separatorChar, '/'));
                    entry.setSize(attrs.size());
                    entry.setTime(attrs.lastModifiedTime().toMillis());
                    zip.putNextEntry(entry);

                    // copy data
                    try (InputStream in = Files.newInputStream(path)) {
                        I.copy(in, zip, false);
                        zip.closeEntry();
                    }
                    break;

                case 3: // walk file
                    add(path);
                    break;

                default: // walk file and directory with visitor
                    return visitor.visitFile(path, attrs);
                }
            }
        }
        return CONTINUE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
        return CONTINUE;
    }

    /**
     * <p>
     * Helper method to test whether the path is acceptable or not.
     * </p>
     *
     * @param path A target path.
     * @return A result.
     */
    private boolean accept(Path path, BasicFileAttributes attr) {
        // File exclusion
        for (PathMatcher matcher : excludes) {
            if (matcher.matches(path)) {
                return false;
            }
        }

        // File inclusion
        return includes.test(path, attr);
    }

    // =======================================================
    // For File Watching Facility
    // =======================================================

    /** The actual file event notification facility. */
    private WatchService service;

    /** The user speecified event listener. */
    private Observer observer;

    /**
     * <p>
     * Sinobu's file event notification facility.
     * </p>
     *
     * @param path A target directory.
     * @param observer A event listener.
     * @param patterns Name matching patterns.
     */
    Visitor(Path path, Observer observer, String... patterns) {
        this(path, null, 6, null, patterns);

        try {
            this.observer = observer;
            this.service = path.getFileSystem().newWatchService();

            // register
            if (patterns.length == 1 && patterns[0].equals("*")) {
                path.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
            } else {
                for (Path dir : I.walkDirectory(path)) {
                    dir.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                }
            }
        } catch (Exception e) {
            throw I.quiet(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        while (true) {
            try {
                WatchKey key = service.take();

                for (WatchEvent event : key.pollEvents()) {
                    // make current modified path
                    Path path = ((Path) key.watchable()).resolve((Path) event.context());
                    BasicFileAttributes attrs = Files.exists(path) ? Files
                            .readAttributes(path, BasicFileAttributes.class) : ZERO;

                    // pattern matching
                    if (accept(from.relativize(path), attrs)) {
                        Agent e = new Agent();
                        e.watch = event;
                        e.object = path;

                        observer.accept(e);

                        if (event.kind() == ENTRY_CREATE) {
                            if (Files.isDirectory(path) && preVisitDirectory(path, attrs) == CONTINUE) {
                                for (Path dir : I.walkDirectory(path)) {
                                    dir.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);
                                }
                            }
                        }
                    }
                }

                // reset key
                key.reset();
            } catch (ClosedWatchServiceException e) {
                break; // Dispose this file watching service.
            } catch (Exception e) {
                // TODO Can we ignore error?
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        I.quiet(service);
        I.quiet(zip);
    }

    // =======================================================
    // For Empty File Attributes
    // =======================================================

    /** The zero time. */
    private static final BasicFileAttributes ZERO = new Agent();
}
