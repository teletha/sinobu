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

import static org.junit.Assert.*;

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
    public static final CleanRoom room = new CleanRoom("file/test01");

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

    /**
     * Helper method to test.
     * 
     * @param expected
     * @param patterns
     */
    private void assertCount(int expected, String... patterns) {
        try {
            // by user
            I.walk(room.root, false, counter, patterns);
            assertEquals(expected, counter.count);

            // by walker
            assertEquals(expected, I.walk(room.root, patterns).size());
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
