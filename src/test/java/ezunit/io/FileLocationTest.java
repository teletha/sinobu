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
package ezunit.io;

import static org.junit.Assert.*;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * @version 2010/02/10 19:44:08
 */
public class FileLocationTest {

    @Test(expected = NullPointerException.class)
    public void nullString() {
        File.locate((String) null);
    }

    @Test(expected = NullPointerException.class)
    public void nullStringMore() {
        File.locate((String) null, (String[]) null);
    }

    @Test(expected = NullPointerException.class)
    public void nullURI() {
        File.locate((URI) null);
    }

    @Test(expected = NullPointerException.class)
    public void nullURL() {
        File.locate((URL) null);
    }

    @Test(expected = NullPointerException.class)
    public void nullFile() {
        File.locate((java.io.File) null);
    }

    @Test
    public void empty() throws Exception {
        File file = File.locate("");
        Path path = Paths.get("");

        assertTrue(file.equals(path));
    }

    @Test
    public void locate() throws Exception {
        File file = File.locate("foo");
        Path path = Paths.get("foo");

        assertTrue(file.equals(path));
    }

    @Test
    public void slash() {
        String filePath = "foo/bar";
        File file = File.locate(filePath);
        Path path = Paths.get(filePath);

        assertTrue(file.equals(path));
    }

    @Test
    public void file() {
        String filePath = "foo/bar.txt";
        File file = File.locate(filePath);
        Path path = Paths.get(filePath);

        assertTrue(file.equals(path));
    }

    @Test
    public void temporary() {
        String filePath = System.getProperty("java.io.tmpdir") + "test";
        File file = File.locate(filePath);
        Path path = Paths.get(filePath);

        assertTrue(file.equals(path));
    }

    @Test
    public void relative() {
        String filePath = "absolute/../relative";
        File file = File.locate(filePath);
        Path path = Paths.get(filePath);

        assertTrue(file.equals(path));
    }
}
