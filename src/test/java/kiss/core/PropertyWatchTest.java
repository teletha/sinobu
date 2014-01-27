/*
 * Copyright (C) 2014 Nameless Production Committee
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
 * @version 2014/01/24 11:23:57
 */
public class PropertyWatchTest {

    @Test
    public void observe() {
        Person person = I.make(Person.class);
        Listener listener = new Listener();

        // start observing
        I.observe(I.mock(person).getFirstName()).subscribe(listener);

        // change property
        person.setFirstName("miku");
        assert listener.event == "miku";
    }

    @Test
    public void primitive() {
        Person person = I.make(Person.class);
        Listener listener = new Listener();

        // start observing
        I.observe(I.mock(person).getAge()).subscribe(listener);

        // change property
        person.setAge(10);
        assert listener.event.equals(10);
    }

    @Test
    public void nest() {
        School school = I.make(School.class);
        school.setName("Ashfood");

        Student student = I.make(Student.class);
        Listener listener = new Listener();

        // start observing
        I.observe(I.mock(student).getSchool().getName()).subscribe(listener);

        // set nested property
        student.setSchool(school);
        assert listener.event == "Ashfood";

        // create another school
        School newSchool = I.make(School.class);
        newSchool.setName("Naoetsu");

        // change nested property
        student.setSchool(newSchool);
        assert listener.event == "Naoetsu";

        // change name property in school
        newSchool.setName("Siritsu Naoetsu");
        assert listener.event == "Siritsu Naoetsu";

        // changing the purged property is not affected
        school.setName("not affect");
        assert listener.event == "Siritsu Naoetsu";
    }

    @Test
    public void generic() {
        GenericStringBean bean = I.make(GenericStringBean.class);
        Listener listener = new Listener();

        // observe
        I.observe(I.mock(bean).getGeneric()).subscribe(listener);

        // change property
        bean.setGeneric("test");
        assert listener.event == "test";
    }

    @Test
    public void unsubscribe() {
        Person person = I.make(Person.class);
        Listener listener = new Listener();

        // start observing
        Disposable disposable = I.observe(I.mock(person).getFirstName()).subscribe(listener);

        // change property
        person.setFirstName("Miku");
        assert listener.event == "Miku";

        // unsubscribe
        disposable.dispose();

        // change property
        person.setFirstName("Mikudayo-");
        assert listener.event == "Miku";
    }

    @Test
    public void unchaged() throws Exception {
        Person person = I.make(Person.class);
        Listener listener = new Listener();

        // start observing
        I.observe(I.mock(person).getFirstName()).subscribe(listener);

        // change property
        person.setFirstName(null);

        assert listener.event == null;
    }

    /**
     * @version 2014/01/24 2:23:13
     */
    private static class Listener implements Observer {

        private Object event;

        /**
         * {@inheritDoc}
         */
        @Override
        public void onNext(Object event) {
            this.event = event;
        }
    }
}
