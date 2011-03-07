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
package ezbean.io;

import static ezunit.Ezunit.*;
import static org.junit.Assert.*;

import java.io.File;

import org.junit.Rule;
import org.junit.Test;

import ezbean.I;
import ezunit.CleanRoom;

/**
 * @version 2011/03/07 18:06:48
 */
public class FilerTest {

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
        Filer.copy(input.toPath(), output.toPath());

        // assert contents
        assertFile(output, read(input));
        assertEquals(input.lastModified(), output.lastModified());
    }

    @Test
    public void copyFileToAbsentFile() throws Exception {
        FilePath input = room.locateFile("file");
        FilePath output = room.locateAbsent("out");

        // copy
        Filer.copy(input.toPath(), output.toPath());

        // assert contents
        assertFile(output, read(input));
        assertEquals(input.lastModified(), output.lastModified());
    }

    @Test
    public void copyFileToDirectory() throws Exception {
        FilePath input = room.locateFile("file");
        FilePath output = room.locateDirectory("directory/out");

        // copy
        Filer.copy(input.toPath(), output.toPath());

        // assert contents
        assertFile(new File(output, "file"), read(input));
    }

    @Test
    public void copyFileToAbsentDirectory() throws Exception {
        FilePath input = room.locateFile("file");
        FilePath output = room.locateAbsent("directory/out");

        // copy
        Filer.copy(input.toPath(), output.toPath());

        // assert contents
        assertFile(output, read(input));
    }

    @Test
    public void copyAbsentFileToAbsentDirectory() throws Exception {
        FilePath input = room.locateFile("file");
        FilePath output = room.locateAbsent("absent/out");

        assertNotSame(input.lastModified(), output.lastModified());

        // copy
        Filer.copy(input.toPath(), output.toPath());

        // assert contents
        assertFile(output, read(input));
        assertEquals(input.lastModified(), output.lastModified());
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
}
