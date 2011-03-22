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
package ezunit;

import static org.junit.Assert.*;

import java.nio.file.Files;

import org.junit.Rule;
import org.junit.Test;

/**
 * @version 2011/03/22 8:53:11
 */
public class PrivateModuleTest {

    @Rule
    public static final PrivateModule module = new PrivateModule(true, false);

    @Rule
    public static final PrivateModule moduleJar = new PrivateModule(true, true);

    @Test
    public void path() throws Exception {
        assertTrue(Files.isDirectory(module.path));
        assertTrue(Files.isRegularFile(moduleJar.path));
    }

    @Test
    public void convert() throws Exception {
        assertNotNull(module.convert(Clazz.class));
        assertNotNull(moduleJar.convert(Clazz.class));
        assertNotSame(Clazz.class, module.convert(Clazz.class));
        assertNotSame(Clazz.class, moduleJar.convert(Clazz.class));
    }

    /**
     * @version 2010/11/07 22:53:30
     */
    private static class Clazz {
    }
}
