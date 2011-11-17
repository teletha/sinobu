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
package ezbean.model;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ezbean.Accessible;
import ezbean.I;
import ezbean.PropertyListener;
import ezbean.sample.bean.Group;
import ezbean.sample.bean.Person;

/**
 * DOCUMENT.
 * 
 * @version 2008/12/11 13:50:17
 */
public class AccessibleTest {

    /**
     * Test method for {@link ezbean.Accessible#ezAccess(java.lang.String)}.
     */
    @Test
    public void testGetAccessibleProperty() {
        Person person = I.make(Person.class);

        assertNotNull(person);
        assertTrue(person instanceof Accessible);

        Model model = Model.load(Person.class);
        Property first = model.getProperty("firstName");
        Property last = model.getProperty("lastName");

        Accessible accessible = (Accessible) person;

        assertEquals(null, accessible.access(first.id, null));
        assertEquals(null, accessible.access(last.id, null));

        person.setFirstName("first");
        person.setLastName("last");

        assertEquals("first", accessible.access(first.id, null));
        assertEquals("last", accessible.access(last.id, null));
    }

    /**
     * Test method for {@link ezbean.Accessible#ezAccess(java.lang.String)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetAccessibleNoProperty() {
        Person person = I.make(Person.class);

        assertNotNull(person);
        assertTrue(person instanceof Accessible);

        Accessible accessible = (Accessible) person;
        assertEquals(null, accessible.access(1000, null));
    }

    /**
     * Test method for {@link ezbean.Accessible#access(java.lang.String, java.lang.Object)}.
     */
    @Test
    public void testSetAccessibleProperty() {
        Person person = I.make(Person.class);

        assertNotNull(person);
        assertTrue(person instanceof Accessible);

        Accessible accessible = (Accessible) person;

        assertEquals(null, person.getFirstName());
        assertEquals(null, person.getLastName());

        Listener listener = new Listener();
        I.observe(I.mock(person).getFirstName(), listener);

        Model model = Model.load(Person.class);
        Property first = model.getProperty("firstName");
        Property last = model.getProperty("lastName");

        accessible.access(first.id + 1, "first");
        accessible.access(last.id + 1, "last");

        assertEquals("first", person.getFirstName());
        assertEquals("last", person.getLastName());
        assertEquals("firstName", listener.property);
    }

    /**
     * Test method for {@link ezbean.Accessible#access(java.lang.String, java.lang.Object)}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSetAccessibleNoProperty() {
        Person person = I.make(Person.class);

        assertNotNull(person);
        assertTrue(person instanceof Accessible);

        Accessible accessible = (Accessible) person;
        accessible.access(1000, "first");
    }

    /**
     * Test method for {@link ezbean.Accessible#ezAccess(java.lang.String)}.
     */
    @Test
    public void testGetAccessibleListProperty() {
        Model model = Model.load(Group.class);
        Property property = model.getProperty("members");

        Group group = I.make(Group.class);
        assertNotNull(group);
        assertTrue(group instanceof Accessible);

        // set member
        Person first = I.make(Person.class);
        first.setFirstName("first");

        Person second = I.make(Person.class);
        second.setFirstName("second");

        List<Person> members = new ArrayList<Person>();
        members.add(first);
        members.add(second);
        group.setMembers(members);

        // members
        members = (List<Person>) ((Accessible) group).access(property.id, null);
        assertNotNull(members);
        assertEquals(2, members.size());
    }

    /**
     * Test method for {@link ezbean.Accessible#context()}.
     */
    @Test
    public void testPropertyContext() throws Exception {
        Person person = I.make(Person.class);
        assertNotNull(person);
        assertTrue(person instanceof Accessible);

        Accessible accessible = (Accessible) person;
        assertNotNull(accessible.context());
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/12/11 13:46:03
     */
    private static class Listener implements PropertyListener<String> {

        private String property;

        /**
         * @see ezbean.PropertyListener#change(java.lang.Object, java.lang.String, java.lang.Object,
         *      java.lang.Object)
         */
        public void change(Object bean, String propertyName, String oldValue, String newValue) {
            property = propertyName;
        }
    }
}
