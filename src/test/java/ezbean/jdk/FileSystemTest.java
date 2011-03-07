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
package ezbean.jdk;

import static org.junit.Assert.*;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * @version 2011/03/07 14:37:56
 */
public class FileSystemTest {

    @Test(expected = NullPointerException.class)
    public void existNull() throws Exception {
        Files.exists(null); // lololol
    }

    @Test
    public void zip() throws Exception {
        FileSystem system = FileSystems.newFileSystem(Paths.get("src/test/resources/ezbean/io/archive/test.zip"), null);
        assertNotNull(system);

        assertTrue(Files.exists(system.getPath("1.txt")));
        assertTrue(Files.notExists(system.getPath("not")));

        int count = 0;

        for (Path child : Files.newDirectoryStream(system.getPath("/"))) {
            if (Files.exists(child)) {
                count++;
            }
        }
        assertEquals(3, count);
    }
}
