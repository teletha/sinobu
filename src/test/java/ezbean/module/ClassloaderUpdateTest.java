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
package ezbean.module;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import ezbean.I;
import ezunit.PrivateModule;

/**
 * @version 2010/11/13 23:11:31
 */
public class ClassloaderUpdateTest {

    @Rule
    public static PrivateModule module = new PrivateModule(true, false);

    @Test
    public void updateClassloader() throws Exception {
        Object object1 = I.make(module.convert(Private.class));

        // reload
        module.load();

        Object object2 = I.make(module.convert(Private.class));

        assertNotSame(object1, object2);
        assertNotSame(object1.getClass(), object2.getClass());
        assertEquals(object1.getClass().getName(), object2.getClass().getName());
        assertNotSame(object1.getClass().getClassLoader(), object2.getClass().getClassLoader());
    }

    /**
     * @version 2010/11/13 23:12:49
     */
    private static class Private {
    }
}
