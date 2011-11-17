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
package ezbean.serialization;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;

import ezbean.I;
import ezbean.sample.bean.Person;
import ezunit.CleanRoom;

/**
 * @version 2011/06/13 12:27:57
 */
public class AbsentPathTest {

    @Rule
    public static final CleanRoom room = new CleanRoom();

    private Person person = new Person();

    @Test
    public void write() throws Exception {
        Path path = room.locateAbsent("file");
        I.write(person, path, false);

        assert Files.exists(path);
    }

    @Test
    public void writeNest() throws Exception {
        Path path = room.locateAbsent("dir/file");
        I.write(person, path, false);

        assert Files.exists(path);
    }

    @Test(expected = NoSuchFileException.class)
    public void read() throws Exception {
        Path path = room.locateAbsent("file");
        I.read(path, person);
    }

    @Test(expected = NoSuchFileException.class)
    public void readNest() throws Exception {
        Path path = room.locateAbsent("dir/file");
        I.read(path, person);
    }
}
