/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.file;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;


import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import testament.CleanRoom;

import kiss.I;

/**
 * @version 2012/01/05 9:20:57
 */
public class PathPatternMatchingTest {

    @Rule
    public static final CleanRoom test01 = new CleanRoom("test01");

    @Rule
    public static final CleanRoom test02 = new CleanRoom("test02");

    private Counter counter = new Counter();

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
        assertDirectoryCount(3, test01);
    }

    @Test
    public void directory2() throws Exception {
        assertDirectoryCount(10, test02);
    }

    @Test
    public void directoryPatternTopLevelPartialWildcard() throws Exception {
        assertDirectoryCount(2, test02, "directory*");
    }

    @Test
    public void directoryPatternTopLevelWildcard() throws Exception {
        assertDirectoryCount(2, test02, "*");
    }

    @Test
    public void directoryPatternWildcard() throws Exception {
        assertDirectoryCount(9, test02, "**");
    }

    @Test
    public void directoryPattern() throws Exception {
        assertDirectoryCount(1, test02, "directory1");
    }

    @Test
    public void directoryPatternDeepWildcard() throws Exception {
        assertDirectoryCount(2, test02, "**/child1");
    }

    @Test
    public void directoryPatternWildcardRight() throws Exception {
        assertDirectoryCount(2, test02, "**/d*");
    }

    @Test
    public void directoryPatternWildcardLeft() throws Exception {
        assertDirectoryCount(3, test02, "**/*2");
    }

    @Test
    public void directoryPatternWildcardBoth() throws Exception {
        assertDirectoryCount(3, test02, "**/*e*");
    }

    @Test
    public void directoryPatternNegative() throws Exception {
        assertDirectoryCount(4, test02, "!directory1");
    }

    /**
     * Helper method to test.
     * 
     * @param expected
     * @param patterns
     */
    private void assertCount(int expected, String... patterns) {
        try {
            // by user
            I.walk(test01.root, counter, patterns);

            assertThat(counter.count, equalTo(expected));

            // by walker
            assert expected == I.walk(test01.root, patterns).size();
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
    private void assertDirectoryCount(int expected, CleanRoom room, String... patterns) {
        try {
            List result = I.walkDirectory(room.root, patterns);

            assertThat(result.size(), equalTo(expected));
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

        /**
         * @see java.nio.file.SimpleFileVisitor#preVisitDirectory(java.lang.Object,
         *      java.nio.file.attribute.BasicFileAttributes)
         */
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
            if (dir.equals(test01.root)) {
                throw new AssertionError("Root directory is passed.");
            }
            return super.preVisitDirectory(dir, attrs);
        }

        /**
         * @see java.nio.file.SimpleFileVisitor#postVisitDirectory(java.lang.Object,
         *      java.io.IOException)
         */
        @Override
        public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
            if (dir.equals(test01.root)) {
                throw new AssertionError("Root directory is passed.");
            }
            return super.postVisitDirectory(dir, exc);
        }
    }
}
