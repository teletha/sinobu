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
package ezbean;

import static org.junit.Assert.*;

import org.junit.Test;

import ezbean.sample.bean.GenericStringBean;
import ezbean.sample.bean.Person;
import ezbean.sample.bean.School;
import ezbean.sample.bean.Student;

/**
 * DOCUMENT.
 * 
 * @version 2008/06/11 18:37:02
 */
public class ObserverTest {

    /**
     * Test <code>null</code>.
     */
    @Test(expected = NullPointerException.class)
    public void testNullPath() {
        // start observing
        I.observe(null, new Listener());
    }

    /**
     * Test empty path.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testEmptyPath() {
        Person person = I.make(Person.class);

        // start observing
        I.observe(I.mock(person), new Listener());
    }

    /**
     * Test <code>null</code> listener.
     */
    @Test(expected = NullPointerException.class)
    public void testNullListener() {
        Person person = I.make(Person.class);

        // start observing
        I.observe(I.mock(person).getFirstName(), null);
    }

    /**
     * Observe single property.
     */
    @Test
    public void testObserver01() {
        Person person = I.make(Person.class);

        Listener listener = new Listener();

        // start observing
        I.observe(I.mock(person).getFirstName(), listener);

        // assert
        assertNull(listener.bean);
        assertNull(listener.propertyName);
        assertNull(listener.oldValue);
        assertNull(listener.newValue);

        // change property
        person.setFirstName("miku");

        // assert
        assertEquals(person, listener.bean);
        assertEquals("firstName", listener.propertyName);
        assertEquals(null, listener.oldValue);
        assertEquals("miku", listener.newValue);
    }

    /**
     * Observe primitive property.
     */
    @Test
    public void testObserver02() {
        Person person = I.make(Person.class);

        Listener listener = new Listener();

        // start observing
        I.observe(I.mock(person).getAge(), listener);

        // assert
        assertNull(listener.bean);
        assertNull(listener.propertyName);
        assertNull(listener.oldValue);
        assertNull(listener.newValue);

        // change property
        person.setAge(10);

        // assert
        assertEquals(person, listener.bean);
        assertEquals("age", listener.propertyName);
        assertEquals(0, listener.oldValue);
        assertEquals(10, listener.newValue);
    }

    /**
     * Nested property.
     */
    @Test
    public void testObserver03() {
        School school = I.make(School.class);
        school.setName("ashfood");

        Student student = I.make(Student.class);

        Listener listener = new Listener();

        // start observing
        I.observe(I.mock(student).getSchool().getName(), listener);

        // assert
        assertNull(listener.bean);
        assertNull(listener.propertyName);
        assertNull(listener.oldValue);
        assertNull(listener.newValue);

        // change property
        student.setSchool(school);

        // assert
        assertEquals(school, listener.bean);
        assertEquals("name", listener.propertyName);
        assertEquals(null, listener.oldValue);
        assertEquals("ashfood", listener.newValue);

        // change property
        School newSchool = I.make(School.class);
        newSchool.setName("new");
        student.setSchool(newSchool);

        // assert
        assertEquals(newSchool, listener.bean);
        assertEquals("name", listener.propertyName);
        assertEquals("ashfood", listener.oldValue);
        assertEquals("new", listener.newValue);

        // change name property in school
        newSchool.setName("change");

        // assert
        assertEquals(newSchool, listener.bean);
        assertEquals("name", listener.propertyName);
        assertEquals("new", listener.oldValue);
        assertEquals("change", listener.newValue);
    }

    @Test
    public void observeGenericProperty() {
        GenericStringBean bean = I.make(GenericStringBean.class);

        Listener listener = new Listener();

        // observe
        I.observe(I.mock(bean).getGeneric(), listener);

        // change property
        bean.setGeneric("test");

        // assert
        assertEquals(bean, listener.bean);
        assertEquals("generic", listener.propertyName);
        assertEquals(null, listener.oldValue);
        assertEquals("test", listener.newValue);
    }

    /**
     * Unobserve single property.
     */
    @Test
    public void testUnobserve01() {
        Person person = I.make(Person.class);

        Listener listener = new Listener();

        // start observing
        Disposable disposable = I.observe(I.mock(person).getFirstName(), listener);

        // assert
        assertNull(listener.newValue);

        // change property
        person.setFirstName("miku");

        // assert
        assertEquals("miku", listener.newValue);

        // unobserve
        disposable.dispose();

        // change property
        person.setFirstName("change");

        // assert
        assertEquals("miku", listener.newValue);
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/11 18:40:03
     */
    private static class Listener implements PropertyListener {

        private Object bean;

        private String propertyName;

        private Object oldValue;

        private Object newValue;

        /**
         * @see ezbean.PropertyListener#change(java.lang.Object, java.lang.String, java.lang.Object,
         *      java.lang.Object)
         */
        public void change(Object bean, String propertyName, Object oldValue, Object newValue) {
            this.bean = bean;
            this.propertyName = propertyName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

    }
}
