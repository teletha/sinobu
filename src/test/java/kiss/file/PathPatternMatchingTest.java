/*
 * Copyright (C) 2014 Nameless Production Committee
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
import java.util.List;
import java.util.function.Consumer;

import kiss.I;

import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;
import antibug.CleanRoom.FileSystemDSL;

/**
 * @version 2014/07/11 8:42:50
 */
public class PathPatternMatchingTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    private Counter counter = new Counter();

    private void pattern1(FileSystemDSL $) {
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

    private void pattern2(FileSystemDSL $) {
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

    @Test
    public void all() throws Exception {
        assertCount(9);
    }

    @Test
    public void includeWildcardLeft() throws Exception {
        assertCount(1, "*.file");
    }

    @Test
    public void includeWildcardsLeft() throws Exception {
        assertCount(3, "**.file");
    }

    @Test
    public void includeWildcardRight() throws Exception {
        assertCount(3, "0*");
    }

    @Test
    public void includeWildcardsRight() throws Exception {
        assertCount(6, "d**");
    }

    @Test
    public void includeWildcardBoth() throws Exception {
        assertCount(2, "*1*");
    }

    @Test
    public void includeWildcardsBoth() throws Exception {
        assertCount(7, "**1**");
    }

    @Test
    public void excludeWildcardLeft() throws Exception {
        assertCount(8, "!*.file");
    }

    @Test
    public void excludeWildcardsLeft() throws Exception {
        assertCount(6, "!**.file");
    }

    @Test
    public void excludeWildcardRight() throws Exception {
        assertCount(6, "!0*");
    }

    @Test
    public void excludeWildcardsRight() throws Exception {
        assertCount(3, "!d**");
    }

    @Test
    public void excludeWildcardBoth() throws Exception {
        assertCount(7, "!*1*");
    }

    @Test
    public void excludeWildcardsBoth() throws Exception {
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
    public void wildcardOnly() throws Exception {
        assertCount(3, "*");
    }

    @Test
    public void excludeDirectory() {
        assertCount(6, "!directory1/**");
    }

    @Test
    public void directory1() throws Exception {
        assertDirectoryCount(3, this::pattern1);
    }

    @Test
    public void directory2() throws Exception {
        assertDirectoryCount(10, this::pattern2);
    }

    @Test
    public void directoryPatternTopLevelPartialWildcard() throws Exception {
        assertDirectoryCount(2, this::pattern2, "directory*");
    }

    @Test
    public void directoryPatternTopLevelWildcard() throws Exception {
        assertDirectoryCount(2, this::pattern2, "*");
    }

    @Test
    public void directoryPatternWildcard() throws Exception {
        assertDirectoryCount(9, this::pattern2, "**");
    }

    @Test
    public void directoryPattern() throws Exception {
        assertDirectoryCount(1, this::pattern2, "directory1");
    }

    @Test
    public void directoryPatternDeepWildcard() throws Exception {
        assertDirectoryCount(2, this::pattern2, "**/child1");
    }

    @Test
    public void directoryPatternWildcardRight() throws Exception {
        assertDirectoryCount(2, this::pattern2, "**/d*");
    }

    @Test
    public void directoryPatternWildcardLeft() throws Exception {
        assertDirectoryCount(3, this::pattern2, "**/*2");
    }

    @Test
    public void directoryPatternWildcardBoth() throws Exception {
        assertDirectoryCount(3, this::pattern2, "**/*e*");
    }

    @Test
    public void directoryPatternNegative() throws Exception {
        assertDirectoryCount(4, this::pattern2, "!directory1");
    }

    /**
     * Helper method to test.
     * 
     * @param expected
     * @param patterns
     */
    private void assertCount(int expected, String... patterns) {
        room.with(this::pattern1);

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
     * 
     * @param expected
     * @param depth
     * @param includeFilesOnly
     * @param patterns
     */
    private void assertDirectoryCount(int expected, Consumer<FileSystemDSL> pattern, String... patterns) {
        room.with(pattern);

        try {
            List result = I.walkDirectory(room.root, patterns);
            assert result.size() == expected;
        } finally {
            counter.count = 0;
        }
    }

    /**
     * @version 2011/03/10 17:59:06
     */
    private static class Counter extends SimpleFileVisitor<Path> {

        private int count = 0;

        /**
         * @see java.nio.file.SimpleFileVisitor#visitFile(java.lang.Object,
         *      java.nio.file.attribute.BasicFileAttributes)
         */
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            count++;
            return super.visitFile(file, attrs);
        }
    }
}
