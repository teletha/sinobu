/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.file;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.Rule;
import org.junit.Test;

import ezbean.I;
import ezunit.CleanRoom;

/**
 * @version 2012/01/05 14:03:23
 */
public class PatternMatchingFileTest {

    @Rule
    public static final CleanRoom room = new CleanRoom("test01");

    private Counter counter = new Counter();

    @Test
    public void none() throws Exception {
        assertCount(9);
    }

    @Test
    public void wildcard() throws Exception {
        assertCount(9, "**");
    }

    @Test
    public void wildcardWithLeftCharacter() throws Exception {
        assertCount(6, "**01*");
    }

    @Test
    public void wildcardWithRightCharacter() throws Exception {
        assertCount(6, "**.txt");
    }

    @Test
    public void boundingWildcardOnFirstLevel() throws Exception {
        assertCount(3, "*");
    }

    @Test
    public void boundingWildcardOnSecondLevel() throws Exception {
        assertCount(6, "*/*");
    }

    @Test
    public void boundingWildcardOnSecondLevelWithLeftCharacter() throws Exception {
        assertCount(4, "*/01*");
    }

    @Test
    public void boundingWildcardOnSecondLevelWithRightCharacter() throws Exception {
        assertCount(4, "*/*.txt");
    }

    @Test
    public void characterMatcher() throws Exception {
        assertCount(2, "0?.txt");
    }

    @Test
    public void characterRangeMatcher() throws Exception {
        assertCount(2, "0[1-2].txt");
    }

    @Test
    public void characterRangeMatcherNegative() throws Exception {
        assertCount(1, "0[!2].txt");
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
            I.walk(room.root, counter, patterns);

            assertThat(counter.count, equalTo(expected));

            // by walker
            assertThat(I.walk(room.root, patterns).size(), equalTo(expected));
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
            if (dir.equals(room.root)) {
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
            if (dir.equals(room.root)) {
                throw new AssertionError("Root directory is passed.");
            }
            return super.postVisitDirectory(dir, exc);
        }
    }
}
