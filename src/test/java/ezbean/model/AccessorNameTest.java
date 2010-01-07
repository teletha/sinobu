/*
 * Copyright (C) 2010 Nameless Production Committee.
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
package ezbean.model;

import static org.junit.Assert.*;


import org.junit.Test;

import ezbean.model.Model;
import ezbean.model.Property;
import ezbean.sample.bean.Person;
import ezbean.sample.bean.Primitive;


/**
 * DOCUMENT.
 * 
 * @version 2008/11/21 16:51:56
 */
public class AccessorNameTest {

    /**
     * Decide a name of accessor method.
     */
    @Test
    public void normal() throws Exception {
        Model model = Model.load(Person.class);
        assertNotNull(model);

        Property property = model.getProperty("age");

        assertEquals("getAge", property.getter.getName());
        assertEquals("setAge", property.setter.getName());
    }

    /**
     * Decide a name of accessor method.
     */
    @Test
    public void primitive() throws Exception {
        Model model = Model.load(Primitive.class);
        assertNotNull(model);

        Property property = model.getProperty("boolean");

        assertEquals("isBoolean", property.getter.getName());
        assertEquals("setBoolean", property.setter.getName());

        property = model.getProperty("int");

        assertEquals("getInt", property.getter.getName());
        assertEquals("setInt", property.setter.getName());
    }
}
