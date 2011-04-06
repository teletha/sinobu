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
package ezbean;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.Rule;
import org.junit.Test;

import ezunit.CleanRoom;

/**
 * @version 2011/03/10 17:57:17
 */
public class PathPatternMatchingTest {

    @Rule
    public static final CleanRoom test01 = new CleanRoom("file/test01");

    @Rule
    public static final CleanRoom test02 = new CleanRoom("file/test02");

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
    public void mix() {
        assertCount(2, "**.txt", "!directory**");
    }

    @Test
    public void mix2() {
        assertCount(3, "directory1/**");
    }

    @Test
    public void excludeDirectory() {
        assertCount(6, "!directory1/**");
    }

    @Test
    public void depth1() throws Exception {
        assertCount(3, 1, true, test01);
    }

    @Test
    public void depth2() throws Exception {
        assertCount(0, 1, true, test02);
    }

    @Test
    public void depthDeeply1() throws Exception {
        assertCount(2, 2, true, test02);
    }

    @Test
    public void depthDeeply2() throws Exception {
        assertCount(5, 3, true, test02);
    }

    @Test
    public void depthZero() throws Exception {
        assertCount(9, 0, true, test01);
    }

    @Test
    public void depthNegative() throws Exception {
        assertCount(9, -1, true, test01);
    }

    @Test
    public void depthMAX() throws Exception {
        assertCount(9, Integer.MAX_VALUE, true, test01);
    }

    @Test
    public void directory1() throws Exception {
        assertCount(2, 0, false, test01);
    }

    @Test
    public void directory2() throws Exception {
        assertCount(9, 0, false, test02);
    }

    @Test
    public void directoryDepth() throws Exception {
        assertCount(2, 1, false, test02);
    }

    @Test
    public void directoryDeepDepth1() throws Exception {
        assertCount(6, 2, false, test02);
    }

    @Test
    public void directoryDeepDepth2() throws Exception {
        assertCount(8, 3, false, test02);
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
            assert expected == counter.count;

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
    private void assertCount(int expected, int depth, boolean includeFilesOnly, CleanRoom room, String... patterns) {
        try {
            assert expected == I.walk(room.root, depth, includeFilesOnly, patterns).size();
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
