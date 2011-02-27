/*
 * Copyright (C) 2010 Nameless Production Committee.
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
package ezbean.io;

import static ezunit.Ezunit.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import ezbean.I;
import ezunit.CleanRoom;
import ezunit.Ezunit;

/**
 * @version 2011/02/15 15:48:47
 */
public class FilesTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    @Test
    public void copyFileToPresentFile() throws Exception {
        FilePath input = room.locateFile("file");
        FilePath output = room.locateAbsent("out");
        output.createNewFile();

        // assert contents
        assertFile(output, "");
        assertNotSame(input.lastModified(), output.lastModified());

        // copy
        ((FilePath) input).copyTo(output);

        // assert contents
        assertFile(output, read(input));
        assertEquals(input.lastModified(), output.lastModified());
    }

    @Test
    public void copyToAbsentFileInPresentDirectory() throws Exception {
        FilePath input = room.locateFile("file");
        FilePath output = room.locateAbsent("directory/out");

        // copy
        ((FilePath) input).copyTo(output);

        // assert contents
        assertFile(output, read(input));
    }

    @Test
    public void copyFileToAbsentFileInAbsentDirectory() throws Exception {
        FilePath input = room.locateFile("file");
        FilePath output = room.locateAbsent("absent/out");

        assertNotSame(input.lastModified(), output.lastModified());

        // copy
        ((FilePath) input).copyTo(output);

        // assert contents
        assertFile(output, read(input));
        assertEquals(input.lastModified(), output.lastModified());
    }

    /**
     * Directory copy to present directory which is empty.
     */
    @Test
    public void copyDirectoryToPresentDirectory() throws Exception {
        FilePath input = room.locateDirectory("directory");
        FilePath output = room.locateAbsent("out");
        output.mkdirs();

        ((FilePath) input).copyTo(output);

        // assert contents
        assertFile(I.locate(output, "directory/1"), "1");
        assertDirectory(I.locate(output, "directory/child"));
        assertFile(I.locate(output, "directory/child/a"), "a");
        assertEquals(input.lastModified(), new File(output, "directory").lastModified());
    }

    /**
     * Directory copy to present directory with filter.
     */
    @Test
    public void copyDirectoryToPresentDirectoryWithFilter() throws Exception {
        FilePath input = room.locateDirectory("directory");
        FilePath output = room.locateAbsent("out");
        output.mkdirs();

        FileSystem.copy(input, output, new FileFilter() {

            /**
             * @see java.io.FileFilter#accept(java.io.File)
             */
            public boolean accept(File file) {
                return file.isFile();
            }
        });

        // assert contents
        assertFile(I.locate(output, "directory/file"), "some contents");
        assertFalse(new File(output, "directory/child").exists());
        assertEquals(input.lastModified(), new File(output, "directory").lastModified());
    }

    @Rule
    public static final MatchSet set1 = new MatchSet("01");

    @Test
    public void all() throws Exception {
        set1.assertMatching(9);
    }

    @Test
    public void includeFile() throws Exception {
        set1.set.include("**.txt");
        set1.assertMatching(6);
    }

    @Test
    public void includeFileWildcard() throws Exception {
        set1.set.include("**02.*");
        set1.assertMatching(3);
    }

    @Test
    public void includeFiles() throws Exception {
        set1.set.include("**.txt", "**.file");
        set1.assertMatching(9);
    }

    @Test
    public void includeDuplicatedFiles() throws Exception {
        set1.set.include("**.txt", "02.**");
        set1.assertMatching(6);
    }

    @Test
    public void includeDirectory() throws Exception {
        set1.set.include("use/**");
        set1.assertMatching(3);
    }

    @Test
    public void includeDirectoryWildcard() throws Exception {
        set1.set.include("use*/**");
        set1.assertMatching(6);
    }

    @Test
    public void excludeDirectory() throws Exception {
        set1.set.exclude("use/**");
        set1.assertMatching(6);
    }

    @Test
    public void excludeDirectoryWildcard() throws Exception {
        set1.set.exclude("use*/**");
        set1.assertMatching(3);
    }

    @Test
    public void excludeFile() throws Exception {
        set1.set.exclude("**01.file");
        set1.assertMatching(6);
    }

    @Test
    public void excludeFileWildcard() throws Exception {
        set1.set.exclude("**01.*");
        set1.assertMatching(3);
    }

    @Test
    @Ignore
    public void delete() {
        set1.assertExist("01.file", "use");
        set1.set.delete();
        set1.assertNotExist("01.file", "use");
    }

    @Test
    @Ignore
    public void deleteExclude() {
        set1.assertExist("01.file", "use", "useless");
        set1.set.exclude("use/**").delete();
        set1.assertExist("use");
        set1.assertNotExist("01.file", "useless");
    }

    @Test
    @Ignore
    public void deleteExcludeFile() {
        set1.assertExist("01.file", "use", "useless");
        set1.set.exclude("**/*.txt").delete();
        set1.assertExist("use", "useless");
        set1.assertNotExist("01.file");
    }

    /**
     * @version 2011/02/15 15:48:53
     */
    private static final class MatchSet extends CleanRoom implements FileVisitor<Path> {

        /** The target file set. */
        private final FilePath set;

        /** The root directory. */
        private final java.io.File root;

        /** The matching file counter. */
        private List<Integer> numbers = new ArrayList();

        /**
         * 
         */
        private MatchSet(String path) {
            super(Ezunit.locatePackage(FilesTest.class) + "/" + path);

            root = locateDirectory("");
            set = new FilePath(root.getAbsolutePath());
        }

        /**
         * @see ezunit.ReusableRule#before(java.lang.reflect.Method)
         */
        @Override
        protected void before(Method method) throws Exception {
            super.before(method);

            set.reset();
            numbers.clear();
        }

        private void assertExist(String... paths) {
            for (String path : paths) {
                try {
                    locateFile(path);
                } catch (AssertionError e) {
                    locateDirectory(path);
                }
            }
        }

        private void assertNotExist(String... paths) {
            for (String path : paths) {
                locateAbsent(path);
            }
        }

        /**
         * <p>
         * Assert the count of the matching files.
         * </p>
         * 
         * @param expected
         */
        private void assertMatching(int expected) {
            try {
                set.scan(this);

                assertEquals(expected, numbers.size());
            } finally {
                numbers.clear();
            }
        }

        /**
         * @see java.nio.file.FileVisitor#postVisitDirectory(java.lang.Object, java.io.IOException)
         */
        public FileVisitResult postVisitDirectory(Path path, IOException attributes) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        /**
         * @see java.nio.file.FileVisitor#preVisitDirectory(java.lang.Object,
         *      java.nio.file.attribute.BasicFileAttributes)
         */
        public FileVisitResult preVisitDirectory(Path path, BasicFileAttributes attributes) throws IOException {
            return FileVisitResult.CONTINUE;
        }

        /**
         * @see java.nio.file.FileVisitor#visitFile(java.lang.Object,
         *      java.nio.file.attribute.BasicFileAttributes)
         */
        public FileVisitResult visitFile(Path path, BasicFileAttributes attributes) throws IOException {
            String name = path.getFileName().toString();
            int index = name.lastIndexOf('.');
            Integer number = Integer.parseInt(name.substring(0, index));
            numbers.add(number);

            return FileVisitResult.CONTINUE;
        }

        /**
         * @see java.nio.file.FileVisitor#visitFileFailed(java.lang.Object, java.io.IOException)
         */
        public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
            return FileVisitResult.CONTINUE;
        }
    }
}
