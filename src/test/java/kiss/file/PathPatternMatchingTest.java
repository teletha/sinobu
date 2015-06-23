/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;
import antibug.CleanRoom.FileSystemDSL;
import kiss.I;

/**
 * @version 2015/06/23 21:12:30
 */
public class PathPatternMatchingTest {

    @Rule
    @ClassRule
    public static final CleanRoom room = new CleanRoom();

    private Counter counter = new Counter();

    private void structure1(FileSystemDSL $) {
        $.dir("directory1", () -> {
            $.file("01.file");
            $.file("01.txt");
            $.file("02.txt");
        });
        $.dir("directory2", () -> {
            $.file("01.file");
            $.file("01.txt");
            $.file("02.txt");
        });
        $.file("01.file");
        $.file("01.txt");
        $.file("02.txt");
    }

    private void structure2(FileSystemDSL $) {
        $.dir("directory1", () -> {
            $.dir("child1", () -> {
                $.dir("decendant1", () -> {
                    $.dir("lowest", () -> {
                        $.file("01.txt");
                    });
                });
                $.dir("decendant2", () -> {
                    $.file("01.txt");
                });
            });

            $.dir("child2", () -> {
                $.file("01.txt");
            });

            $.file("01.txt");
            $.file("02.txt");
        });
        $.dir("directory2", () -> {
            $.dir("child1", () -> {
                $.file("01.txt");
            });

            $.dir("child2", () -> {
                $.file("02.txt");
            });
        });
    }

    private void archive1(FileSystemDSL $) {
        $.zip("archive", () -> {
            structure1($);
        });
    }

    @Test
    public void all() {
        assertCount(9);
    }

    @Test
    public void includeWildcardLeft() {
        assertCount(1, "*.file");
    }

    @Test
    public void includeWildcardsLeft() {
        assertCount(3, "**.file");
    }

    @Test
    public void includeWildcardRight() {
        assertCount(3, "0*");
    }

    @Test
    public void includeWildcardsRight() {
        assertCount(6, "d**");
    }

    @Test
    public void includeWildcardBoth() {
        assertCount(2, "*1*");
    }

    @Test
    public void includeWildcardsBoth() {
        assertCount(7, "**1**");
    }

    @Test
    public void excludeWildcardLeft() {
        assertCount(8, "!*.file");
    }

    @Test
    public void excludeWildcardsLeft() {
        assertCount(6, "!**.file");
    }

    @Test
    public void excludeWildcardRight() {
        assertCount(6, "!0*");
    }

    @Test
    public void excludeWildcardsRight() {
        assertCount(3, "!d**");
    }

    @Test
    public void excludeWildcardBoth() {
        assertCount(7, "!*1*");
    }

    @Test
    public void excludeWildcardsBoth() {
        assertCount(2, "!**1**");
    }

    @Test
    public void multiple() {
        assertCount(2, "**.txt", "!directory**");
    }

    @Test
    public void deep() {
        assertCount(3, "directory1/**");
    }

    @Test
    public void wildcardOnly() {
        assertCount(3, "*");
    }

    @Test
    public void excludeDirectory() {
        assertCount(6, "!directory1/**");
    }

    @Test
    public void directory1() {
        assertDirectoryCount(3, this::structure1);
    }

    @Test
    public void directory2() {
        assertDirectoryCount(10, this::structure2);
    }

    @Test
    public void directoryPatternTopLevelPartialWildcard() {
        assertDirectoryCount(2, this::structure2, "directory*");
    }

    @Test
    public void directoryPatternTopLevelWildcard() {
        assertDirectoryCount(2, this::structure2, "*");
    }

    @Test
    public void directoryPatternWildcard() {
        assertDirectoryCount(9, this::structure2, "**");
    }

    @Test
    public void directoryPattern() {
        assertDirectoryCount(1, this::structure2, "directory1");
    }

    @Test
    public void directoryPatternDeepWildcard() {
        assertDirectoryCount(2, this::structure2, "**/child1");
    }

    @Test
    public void directoryPatternWildcardRight() {
        assertDirectoryCount(2, this::structure2, "**/d*");
    }

    @Test
    public void directoryPatternWildcardLeft() {
        assertDirectoryCount(3, this::structure2, "**/*2");
    }

    @Test
    public void directoryPatternWildcardBoth() {
        assertDirectoryCount(3, this::structure2, "**/*e*");
    }

    @Test
    public void directoryPatternNegative() {
        assertDirectoryCount(4, this::structure2, "!directory1");
    }

    @Test
    public void archive() {
        assertArchiveCount(9);
    }

    @Test
    public void archiveIncludeWildcardLeft() {
        assertArchiveCount(1, "*.file");
    }

    @Test
    public void archiveIncludeWildcardsLeft() {
        assertArchiveCount(3, "**.file");
    }

    @Test
    public void archiveIncludeWildcardRight() {
        assertArchiveCount(3, "0*");
    }

    @Test
    public void archiveIncludeWildcardsRight() {
        assertArchiveCount(6, "d**");
    }

    @Test
    public void archiveIncludeWildcardBoth() {
        assertArchiveCount(2, "*1*");
    }

    @Test
    public void archiveIncludeWildcardsBoth() {
        assertArchiveCount(7, "**1**");
    }

    @Test
    public void archiveExcludeWildcardLeft() {
        assertArchiveCount(8, "!*.file");
    }

    @Test
    public void archiveExcludeWildcardsLeft() {
        assertArchiveCount(6, "!**.file");
    }

    @Test
    public void archiveExcludeWildcardRight() {
        assertArchiveCount(6, "!0*");
    }

    @Test
    public void archiveExcludeWildcardsRight() {
        assertArchiveCount(3, "!d**");
    }

    @Test
    public void archiveExcludeWildcardBoth() {
        assertArchiveCount(7, "!*1*");
    }

    @Test
    public void archiveExcludeWildcardsBoth() {
        assertArchiveCount(2, "!**1**");
    }

    @Test
    public void aarchiveMultiple() {
        assertArchiveCount(2, "**.txt", "!directory**");
    }

    @Test
    public void archiveDeep() {
        assertArchiveCount(3, "directory1/**");
    }

    @Test
    public void archiveWildcardOnly() {
        assertArchiveCount(3, "*");
    }

    @Test
    public void archiveExcludeDirectory() {
        assertArchiveCount(6, "!directory1/**");
    }

    /**
     * Helper method to test.
     */
    private void assertCount(int expected, String... patterns) {
        assertCount(expected, this::structure1, patterns);
    }

    /**
     * Helper method to test.
     */
    private void assertCount(int expected, Consumer<FileSystemDSL> pattern, String... patterns) {
        room.with(pattern);

        try {
            // by user
            I.walk(room.root, counter, patterns);
            assert counter.count == expected;

            // by walker
            assert expected == I.walk(room.root, patterns).size();
        } finally {
            counter.count = 0;
        }
    }

    /**
     * Helper method to test.
     */
    private void assertDirectoryCount(int expected, Consumer<FileSystemDSL> pattern, String... patterns) {
        room.with(pattern);

        assert I.walkDirectory(room.root, patterns).size() == expected;
    }

    /**
     * Helper method to test.
     */
    private void assertArchiveCount(int expected, String... patterns) {
        assertArchiveCount(expected, this::archive1, patterns);
    }

    /**
     * Helper method to test.
     */
    private void assertArchiveCount(int expected, Consumer<FileSystemDSL> pattern, String... patterns) {
        room.with(pattern);

        try {
            Path zip = room.locateArchive("archive.zip");

            // by user
            I.walk(zip, counter, patterns);
            assert counter.count == expected;

            // by walker
            assert expected == I.walk(zip, patterns).size();
        } finally {
            counter.count = 0;
        }
    }

    /**
     * @version 2015/06/23 21:12:19
     */
    private static class Counter extends SimpleFileVisitor<Path> {

        private int count = 0;

        /**
         * {@inheritDoc}
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            count++;
            return super.visitFile(file, attrs);
        }
    }
}
