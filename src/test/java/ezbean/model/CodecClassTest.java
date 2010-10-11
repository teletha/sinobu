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

import ezunit.PrivateModule;

/**
 * @version 2010/02/04 10:29:49
 */
public class CodecClassTest {

    @Rule
    public static final PrivateModule module = new PrivateModule(true, false);

    @Test
    public void systemClass() throws Exception {
        CodecClass codec = new CodecClass();
        Class clazz = codec.decode("java.lang.String");
        assertNotNull(clazz);
        assertEquals("java.lang.String", codec.encode(clazz));
    }

    @Test
    public void moduleClass() throws Exception {
        String fqcn = module.forName(Private.class);
        assertNotSame(Private.class.getName(), fqcn);

        CodecClass codec = new CodecClass();
        Class clazz = codec.decode(fqcn);
        assertNotNull(clazz);
        assertNotSame(Private.class, clazz);
        assertEquals(fqcn, codec.encode(clazz));
    }

    /**
     * @version 2010/02/04 9:43:23
     */
    private static class Private {
    }
}
