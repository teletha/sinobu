/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.icy.model;

import java.util.function.UnaryOperator;

import kiss.icy.Gender;
import kiss.icy.Lens;
import kiss.icy.ModelOperator;
import kiss.icy.Setter;

/**
 * @version 2015/04/24 16:34:57
 */
public class Person {

    /** The lens for leader property. */
    private static final Lens<Person, String> NAME = Lens
            .of(model -> model.name, (model, value) -> new Person(value, model.age, model.gender));

    /** The lens for age property. */
    private static final Lens<Person, Integer> AGE = Lens
            .of(model -> model.age, (model, value) -> new Person(model.name, value, model.gender));

    /** The name getter. */
    public final String name;

    /** The age getter. */
    public final int age;

    /** The gender getter. */
    public final Gender gender;

    /**
     * @param name
     * @param age
     * @param gender
     */
    private Person(String name, int age, Gender gender) {
        this.name = name;
        this.age = age;
        this.gender = gender;
    }

    /**
     * @version 2015/04/24 16:41:14
     */
    public static final class Builder {

        /** The current building instance. */
        private PersonDef instance;

        /**
         * <p>
         * Assign name property.
         * </p>
         * 
         * @param name A property to assign.
         * @return Chainable API.
         */
        public Builder name(String name) {
            instance.name = name;

            return this;
        }

        /**
         * <p>
         * Assign age property.
         * </p>
         * 
         * @param age A property to assign.
         * @return Chainable API.
         */
        public Builder age(int age) {
            instance.age = age;

            return this;
        }

        /**
         * <p>
         * Assign gender property.
         * </p>
         * 
         * @param gender A property to assign.
         * @return Chainable API.
         */
        public Builder gender(Gender gender) {
            instance.gender = gender;

            return this;
        }

        /**
         * <p>
         * Create new immutable model with assigned properties.
         * </p>
         * 
         * @return A created immutable model.
         */
        public Person build() {
            return new Person(instance.name, instance.age, instance.gender);
        }
    }

    /**
     * @version 2015/04/24 16:52:22
     */
    public static final class Operator<M> extends ModelOperator<M, Person> {

        /**
         * @param lens
         */
        private Operator(Lens<M, Person> lens) {
            super(lens);
        }

        /**
         * <p>
         * Setter kind.
         * </p>
         * 
         * @param name
         * @return
         */
        public Setter<M> name(String name) {
            return model -> lens.then(NAME).set(model, name);
        }

        /**
         * <p>
         * Setter kind.
         * </p>
         * 
         * @param name
         * @return
         */
        public Setter<M> name(UnaryOperator<String> name) {
            return model -> lens.then(NAME).set(model, name);
        }
    }
}
