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
package ezbean;

import static org.junit.Assert.*;


import org.junit.Test;

import ezbean.I;
import ezbean.Manageable;
import ezbean.Prototype;

/**
 * DOCUMENT.
 * 
 * @version 2008/05/30 22:49:10
 */
public class PrototypeTest {

    /**
     * Test {@link Prototype}.
     */
    @Test
    public void testResolve() {
        PrototypeClass instance1 = I.make(PrototypeClass.class);
        assertNotNull(instance1);

        PrototypeClass instance2 = I.make(PrototypeClass.class);
        assertNotNull(instance2);

        assertNotSame(instance1, instance2);
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/05/30 22:50:38
     */
    @Manageable(lifestyle = Prototype.class)
    private static class PrototypeClass {
    }
}
