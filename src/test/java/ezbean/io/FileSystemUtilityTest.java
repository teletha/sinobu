/*
 * Copyright (C) 2011 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean.io;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;

import ezunit.CleanRoom;

/**
 * DOCUMENT.
 * 
 * @version 2008/08/26 5:25:31
 */
public class FileSystemUtilityTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    @Test
    public void fileEquality() throws Exception {
        File one = new File("a");
        File other = new File("a/../a");

        assertFalse(one.equals(other));
        assertFalse(other.equals(one));
    }

    /**
     * Create temporary file.
     */
    @Test
    public void testCreateTemporaryAsFile() throws IOException {
        File file = FilePath.createTemporary();
        assertFalse(file.exists());
        assertTrue(file.createNewFile());
    }

    /**
     * Create temporary directory.
     */
    @Test
    public void testCreateTemporaryAsDirectory() throws IOException {
        File file = FilePath.createTemporary();
        assertFalse(file.exists());
        assertTrue(file.mkdir());
    }

    @Test
    public void dontCreateDuplicatedName() {
        File file1 = FilePath.createTemporary();
        File file2 = FilePath.createTemporary();
        assertNotSame(file1.getName(), file2.getName());
    }
}
