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

import org.junit.Test;

import ezbean.sample.bean.GenericStringBean;
import ezbean.sample.bean.Person;
import ezbean.sample.bean.School;
import ezbean.sample.bean.Student;

/**
 * @version 2011/03/22 16:39:53
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
        assert listener.bean == null;
        assert listener.propertyName == null;
        assert listener.oldValue == null;
        assert listener.newValue == null;

        // change property
        person.setFirstName("miku");

        // assert
        assert person == listener.bean;
        assert "firstName" == listener.propertyName;
        assert null == listener.oldValue;
        assert "miku" == listener.newValue;
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
        assert listener.bean == null;
        assert listener.propertyName == null;
        assert listener.oldValue == null;
        assert listener.newValue == null;

        // change property
        person.setAge(10);

        // assert
        assert person == listener.bean;
        assert "age" == listener.propertyName;
        assert listener.oldValue.equals(0);
        assert listener.newValue.equals(10);
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
        assert listener.bean == null;
        assert listener.propertyName == null;
        assert listener.oldValue == null;
        assert listener.newValue == null;

        // change property
        student.setSchool(school);

        // assert
        assert school == listener.bean;
        assert "name" == listener.propertyName;
        assert null == listener.oldValue;
        assert "ashfood" == listener.newValue;

        // change property
        School newSchool = I.make(School.class);
        newSchool.setName("new");
        student.setSchool(newSchool);

        // assert
        assert newSchool == listener.bean;
        assert "name" == listener.propertyName;
        assert "ashfood" == listener.oldValue;
        assert "new" == listener.newValue;

        // change name property in school
        newSchool.setName("change");

        // assert
        assert newSchool == listener.bean;
        assert "name" == listener.propertyName;
        assert "new" == listener.oldValue;
        assert "change" == listener.newValue;
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
        assert bean == listener.bean;
        assert "generic" == listener.propertyName;
        assert null == listener.oldValue;
        assert "test" == listener.newValue;
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
        assert listener.newValue == null;

        // change property
        person.setFirstName("miku");

        // assert
        assert "miku" == listener.newValue;

        // unobserve
        disposable.dispose();

        // change property
        person.setFirstName("change");

        // assert
        assert "miku" == listener.newValue;
    }

    /**
     * @version 2010/03/19 10:40:34
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
