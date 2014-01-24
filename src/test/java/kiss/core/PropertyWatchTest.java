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
import kiss.model.PropertyEvent;
import kiss.sample.bean.GenericStringBean;
import kiss.sample.bean.Person;
import kiss.sample.bean.School;
import kiss.sample.bean.Student;

import org.junit.Test;

/**
 * @version 2014/01/24 11:23:57
 */
public class PropertyWatchTest {

    @Test
    public void observe() {
        Person person = I.make(Person.class);
        Listener listener = new Listener();

        // start observing
        I.watch(I.mock(person).getFirstName()).subscribe(listener);

        // change property
        person.setFirstName("miku");

        assert listener.event.getSource() == person;
        assert listener.event.getPropertyName().equals("firstName");
        assert listener.event.getOldValue() == null;
        assert listener.event.getNewValue() == "miku";
    }

    @Test
    public void primitive() {
        Person person = I.make(Person.class);
        Listener listener = new Listener();

        // start observing
        I.watch(I.mock(person).getAge()).subscribe(listener);

        // change property
        person.setAge(10);
        assert listener.event.getSource() == person;
        assert listener.event.getPropertyName().equals("age");
        assert listener.event.getOldValue().equals(0);
        assert listener.event.getNewValue().equals(10);
    }

    @Test
    public void nest() {
        School school = I.make(School.class);
        school.setName("Ashfood");

        Student student = I.make(Student.class);
        Listener listener = new Listener();

        // start observing
        I.watch(I.mock(student).getSchool().getName()).subscribe(listener);

        // set nested property
        student.setSchool(school);
        assert listener.event.getSource() == school;
        assert listener.event.getPropertyName().equals("name");
        assert listener.event.getOldValue() == null;
        assert listener.event.getNewValue() == "Ashfood";

        // create another school
        School newSchool = I.make(School.class);
        newSchool.setName("Naoetsu");

        // change nested property
        student.setSchool(newSchool);
        assert listener.event.getSource() == newSchool;
        assert listener.event.getPropertyName().equals("name");
        assert listener.event.getOldValue() == "Ashfood";
        assert listener.event.getNewValue() == "Naoetsu";

        // change name property in school
        newSchool.setName("Siritsu Naoetsu");
        assert listener.event.getSource() == newSchool;
        assert listener.event.getPropertyName().equals("name");
        assert listener.event.getOldValue() == "Naoetsu";
        assert listener.event.getNewValue() == "Siritsu Naoetsu";
    }

    @Test
    public void generic() {
        GenericStringBean bean = I.make(GenericStringBean.class);
        Listener listener = new Listener();

        // observe
        I.watch(I.mock(bean).getGeneric()).subscribe(listener);

        // change property
        bean.setGeneric("test");
        assert listener.event.getSource() == bean;
        assert listener.event.getPropertyName().equals("generic");
        assert listener.event.getOldValue() == null;
        assert listener.event.getNewValue() == "test";
    }

    @Test
    public void unsubscribe() {
        Person person = I.make(Person.class);
        Listener listener = new Listener();

        // start observing
        Disposable disposable = I.watch(I.mock(person).getFirstName()).subscribe(listener);

        // change property
        person.setFirstName("Miku");
        assert listener.event.getSource() == person;
        assert listener.event.getPropertyName().equals("firstName");
        assert listener.event.getOldValue() == null;
        assert listener.event.getNewValue() == "Miku";

        // unsubscribe
        disposable.dispose();

        // change property
        person.setFirstName("Mikudayo-");
        assert listener.event.getSource() == person;
        assert listener.event.getPropertyName().equals("firstName");
        assert listener.event.getOldValue() == null;
        assert listener.event.getNewValue() == "Miku";
    }

    @Test
    public void unchaged() throws Exception {
        Person person = I.make(Person.class);
        Listener listener = new Listener();

        // start observing
        I.watch(I.mock(person).getFirstName()).subscribe(listener);

        // change property
        person.setFirstName(null);

        assert listener.event == null;
    }

    /**
     * @version 2014/01/24 2:23:13
     */
    private static class Listener implements Observer<PropertyEvent> {

        private PropertyEvent event;

        /**
         * {@inheritDoc}
         */
        @Override
        public void onNext(PropertyEvent event) {
            this.event = event;
        }
    }
}
