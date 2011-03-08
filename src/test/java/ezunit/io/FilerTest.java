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
package ezunit.io;

import static ezunit.Ezunit.*;
import static org.junit.Assert.*;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;

import ezbean.I;
import ezbean.io.FilePath;
import ezunit.CleanRoom;

/**
 * @version 2011/03/07 18:06:48
 */
public class FilerTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    @Test
    public void copyFileToPresentFile() throws Exception {
        Path input = room.locateFile2("file");
        Path output = room.locateFile2("out");

        // assert contents
        assertFilePath(output, "");
        assertNotSame(Files.getLastModifiedTime(input), Files.getLastModifiedTime(output));

        // copy
        Filer.copy(input, output);

        // assert contents
        assertFilePath(output, read(input));
        assertEquals(Files.getLastModifiedTime(input), Files.getLastModifiedTime(output));
    }

    @Test
    public void copyFileToAbsent() throws Exception {
        Path input = room.locateFile2("file");
        Path output = room.locateAbsent2("out");

        // copy
        Filer.copy(input, output);

        // assert contents
        assertFilePath(output, read(input));
        assertEquals(Files.getLastModifiedTime(input), Files.getLastModifiedTime(output));
    }

    @Test
    public void copyFileToDirectory() throws Exception {
        Path input = room.locateFile2("file");
        Path output = room.locateDirectory2("directory/out");

        // copy
        Filer.copy(input, output);

        // assert contents
        assertFilePath(output.resolve(input.getFileName()), read(input));
    }

    @Test(expected = NoSuchFileException.class)
    public void copyDirectoryToPresentFile() throws Exception {
        Path input = room.locateDirectory2("dir");
        Path output = room.locateFile2("file");

        // copy
        Filer.copy(input, output);
    }

    @Test
    public void copyDirectoryToAbsent() throws Exception {
        Path input = room.locateDirectory2("directory");
        Path output = room.locateAbsent2("out");

        // copy
        Filer.copy(input, output);

        // assert contents

        CarbonCopy cc = new CarbonCopy(input, output.resolve(input.getFileName()));
        assertDirectoryPath(output);
        assertEquals(Files.getLastModifiedTime(input), Files.getLastModifiedTime(output));

        input = input.resolve("01.txt");
        output = output.resolve(input.getFileName());
        assertFilePath(output);
        assertEquals(Files.getLastModifiedTime(input), Files.getLastModifiedTime(output));
    }

    /**
     * Directory copy to present directory which is empty.
     */
    @Test
    public void copyDirectoryToDirectory() throws Exception {
        FilePath input = room.locateDirectory("directory");
        FilePath output = room.locateAbsent("out");
        output.mkdirs();

        // copy
        Filer.copy(input.toPath(), output.toPath());

        // assert contents
        assertFile(I.locate(output, "directory/1"), "1");
        assertDirectory(I.locate(output, "directory/child"));
        assertFile(I.locate(output, "directory/child/a"), "a");
        assertEquals(input.lastModified(), new File(output, "directory").lastModified());
    }

    /**
     * Directory copy to present directory which is empty.
     */
    @Test
    public void copyDirectoryToAbsentDirectory() throws Exception {
        FilePath input = room.locateDirectory("directory");
        FilePath output = room.locateAbsent("out");

        // copy
        Filer.copy(input.toPath(), output.toPath());

        // assert contents
        assertFile(I.locate(output, "directory/1"), "1");
        assertDirectory(I.locate(output, "directory/child"));
        assertFile(I.locate(output, "directory/child/a"), "a");
        assertEquals(input.lastModified(), new File(output, "directory").lastModified());
    }

    /**
     * <p>
     * Support for simultaneous operations of Path.
     * </p>
     * 
     * @version 2011/03/09 8:23:57
     */
    private static class CarbonCopy {

        /** The all target paths. */
        private final Path[] paths;

        /**
         * @param paths
         */
        private CarbonCopy(Path... paths) {
            this.paths = paths;
        }

    }
}
