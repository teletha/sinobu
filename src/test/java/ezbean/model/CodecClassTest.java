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
package ezbean.model;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import ezbean.module.ModuleTestRule;

/**
 * @version 2010/01/14 12:41:37
 */
public class CodecClassTest {

    @Rule
    public static final ModuleTestRule module = new ModuleTestRule();

    @Test
    public void systemClass() throws Exception {
        CodecClass codec = new CodecClass();
        Class clazz = codec.decode("java.lang.String");
        assertNotNull(clazz);
        assertEquals("java.lang.String", codec.encode(clazz));
    }

    @Test
    public void moduleClass() throws Exception {
        module.load(module.dir);

        CodecClass codec = new CodecClass();
        Class clazz = codec.decode("external.Class1");
        assertNotNull(clazz);
        assertEquals("external.Class1", codec.encode(clazz));
    }
}
