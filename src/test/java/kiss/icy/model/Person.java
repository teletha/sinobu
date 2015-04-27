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
import kiss.icy.Operation;

/**
 * @version 2015/04/24 16:34:57
 */
public class Person {

    /** The lens for leader property. */
    public static final Lens<Person, String> NAME = Lens.of(Person::name, Person::name);

    /** The lens for age property. */
    public static final Lens<Person, Integer> AGE = Lens.of(Person::age, Person::age);

    /** The current model. */
    PersonDef model;

    /**
     * <p>
     * Create model with the specified property holder.
     * </p>
     * 
     * @param model
     */
    private Person() {
    }

    /**
     * <p>
     * Retrieve name property.
     * </p>
     * 
     * @return A name property
     */
    public String name() {
        return model.name;
    }

    /**
     * <p>
     * Create new model with the specified property.
     * </p>
     * 
     * @param value A new property.
     * @return A created model.
     */
    public Person name(String value) {
        if (model.name == value) {
            return this;
        }
        return with(this).name(value).ice();
    }

    /**
     * <p>
     * Retrieve age property.
     * </p>
     * 
     * @return A age property
     */
    public int age() {
        return model.age;
    }

    /**
     * <p>
     * Create new model with the specified property.
     * </p>
     * 
     * @param value A new property.
     * @return A created model.
     */
    public Person age(int value) {
        if (model.age == value) {
            return this;
        }
        return with(this).age(value).ice();
    }

    /**
     * <p>
     * Retrieve gender property.
     * </p>
     * 
     * @return A gender property
     */
    public Gender gender() {
        return model.gender;
    }

    /**
     * <p>
     * Create new model with the specified property.
     * </p>
     * 
     * @param value A new property.
     * @return A created model.
     */
    public Person gender(Gender value) {
        if (model.gender == value) {
            return this;
        }
        return with(this).gender(value).ice();
    }

    /**
     * <p>
     * Create new immutable model.
     * </p>
     * 
     * @return An immutable model.
     */
    public Person ice() {
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return model.name + "  " + model.age + "  " + model.gender;
    }

    /**
     * <p>
     * Create new mutable model.
     * </p>
     * 
     * @return An immutable model.
     */
    public Person melt() {
        return new Melty(this);
    }

    /**
     * <p>
     * Create model builder without base model.
     * </p>
     * 
     * @return A new model builder.
     */
    public static final Person with() {
        return with(null);
    }

    /**
     * <p>
     * Create model builder using the specified definition as base model.
     * </p>
     * 
     * @return A new model builder.
     */
    public static final Person with(Person base) {
        return new Melty(base);
    }

    /**
     * @version 2015/04/26 16:49:59
     */
    private static final class Icy extends Person {

        /**
         * 
         */
        private Icy(Person base) {
            model = new PersonDef();

            if (base != null) {
                model.name = base.name();
                model.age = base.age();
                model.gender = base.gender();
            }
        }
    }

    /**
     * @version 2015/04/24 16:41:14
     */
    private static final class Melty extends Person {

        /**
         * @param base
         */
        private Melty(Person base) {
            if (base == null) {
                model = new PersonDef();
            } else {
                model = base.model;
            }
        }

        /**
         * <p>
         * Assign name property.
         * </p>
         * 
         * @param name A property to assign.
         * @return Chainable API.
         */
        @Override
        public Melty name(String name) {
            model.name = name;

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
        @Override
        public Melty age(int age) {
            model.age = age;

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
        @Override
        public Melty gender(Gender gender) {
            model.gender = gender;

            return this;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Person ice() {
            return new Icy(this);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Person melt() {
            return this;
        }
    }

    /**
     * @version 2015/04/24 16:52:22
     */
    public static final class Operator<M> extends ModelOperator<M, Person> {

        /**
         * @param lens
         */
        public Operator(Lens<M, Person> lens) {
            super(lens);
        }

        /**
         * <p>
         * Operation kind.
         * </p>
         * 
         * @param name
         * @return
         */
        public Operation<M> name(String name) {
            return model -> lens.then(NAME).set(model, name);
        }

        /**
         * <p>
         * Operation kind.
         * </p>
         * 
         * @param name
         * @return
         */
        public Operation<M> name(UnaryOperator<String> name) {
            return model -> lens.then(NAME).set(model, name);
        }
    }
}
