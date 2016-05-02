/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.beans.Transient;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

import kiss.I;
import kiss.sample.bean.Person;

/**
 * @version 2016/05/02 9:36:48
 */
public class PropertyTest {

    @Test
    public void accessor() {
        Model<Person> model = Model.of(Person.class);
        Property<Person, Integer> property = model.property("age");

        Person person = I.make(Person.class);
        person.setAge(20);
        assert property.get(person) == 20;
        property.set(person, 10);
        assert person.getAge() == 10;
    }

    @Test
    public void annotation() throws Exception {
        Model model = Model.of(Annotated.class);
        Property property = model.property("name");
        assert property.getAnnotation(KarenBee.class) != null;
        assert property.getAnnotation(Transient.class) == null;

        property = model.property("field");
        assert property.getAnnotation(KarenBee.class) != null;
        assert property.getAnnotation(Transient.class) == null;
    }

    /**
     * @version 2012/03/16 13:34:44
     */
    @SuppressWarnings("unused")
    private static class Annotated {

        @KarenBee
        public int field;

        private String name;

        /**
         * Get the name property of this {@link PropertyTest.Annotated}.
         * 
         * @return The name property.
         */
        protected String getName() {
            return name;
        }

        /**
         * Set the name property of this {@link PropertyTest.Annotated}.
         * 
         * @param name The name value to set.
         */
        @KarenBee
        protected void setName(String name) {
            this.name = name;
        }
    }

    /**
     * 
     */
    @Retention(RetentionPolicy.RUNTIME)
    private static @interface KarenBee {
    }
}
