/**
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
package ezbean.module;

import static org.junit.Assert.*;


import java.security.CodeSource;



import org.junit.Test;

import ezbean.I;
import ezbean.sample.bean.Person;


/**
 * DOCUMENT.
 * 
 * @version 2007/10/15 9:04:33
 */
public class CodeSourceTest {

    @Test
    public void testCodeSource() {
        Person person = I.make(Person.class);
        assertNotSame(Person.class, person.getClass());

        CodeSource source1 = person.getClass().getProtectionDomain().getCodeSource();
        CodeSource source2 = Person.class.getProtectionDomain().getCodeSource();
        assertEquals(source1, source2);
    }
}
