/*
 * Copyright (C) 2013 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import kiss.Disposable;
import kiss.I;
import kiss.Observer;
import kiss.sample.bean.GenericStringBean;
import kiss.sample.bean.Person;
import kiss.sample.bean.School;
import kiss.sample.bean.Student;

import org.junit.Test;

/**
 * @version 2014/01/23 16:11:44
 */
public class PropertyWatchTest {

    @Test(expected = NullPointerException.class)
    public void nullPath() {
        // start observing
        I.watch(null);
    }

    /**
     * Observe single property.
     */
    @Test
    public void observe() {
        Person person = I.make(Person.class);

        Listener listener = new Listener();

        // start observing
        I.watch(I.mock(person).getFirstName()).subscribe(listener);

        // assert
        assert listener.newValue == null;

        // change property
        person.setFirstName("miku");

        // assert
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
        I.watch(I.mock(person).getAge()).subscribe(listener);

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
        I.watch(I.mock(student).getSchool().getName()).subscribe(listener);

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
        I.watch(I.mock(bean).getGeneric()).subscribe(listener);

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
        Disposable disposable = I.watch(I.mock(person).getFirstName()).subscribe(listener);

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
    private static class Listener implements Observer {

        private Object bean;

        private String propertyName;

        private Object oldValue;

        private Object newValue;

        /**
         * {@inheritDoc}
         */
        @Override
        public void onNext(Object value) {
            newValue = value;
        }
    }
}
