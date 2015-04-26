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
    final PersonDef def;

    /**
     * <p>
     * Create model with the specified property holder.
     * </p>
     * 
     * @param def
     */
    private Person(PersonDef def) {
        this.def = def;
    }

    /**
     * <p>
     * Retrieve name property.
     * </p>
     * 
     * @return A name property
     */
    public String name() {
        return def.name;
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
        if (def.name == value) {
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
        return def.age;
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
        if (def.age == value) {
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
        return def.gender;
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
        if (def.gender == value) {
            return this;
        }
        return with(this).gender(value).ice();
    }

    /**
     * <p>
     * Retrieve club property.
     * </p>
     * 
     * @return A club property
     */
    public Club club() {
        return def.club;
    }

    /**
     * <p>
     * Create new model with the specified property.
     * </p>
     * 
     * @param value A new property.
     * @return A created model.
     */
    public Person club(Club value) {
        if (def.club == value) {
            return this;
        }
        return with(this).club(value).ice();
    }

    /**
     * <p>
     * Create new immutable model.
     * </p>
     * 
     * @return An immutable model.
     */
    public final Person ice() {
        return this instanceof Melty ? new Person(def) : this;
    }

    /**
     * <p>
     * Create new mutable model.
     * </p>
     * 
     * @return An immutable model.
     */
    public final Person melt() {
        return this instanceof Melty ? this : new Melty(this);
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
     * @version 2015/04/24 16:41:14
     */
    private static final class Melty extends Person {

        /**
         * @param model
         */
        private Melty(Person model) {
            super(model == null ? new PersonDef() : model.def);
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
            def.name = name;

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
            def.age = age;

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
            def.gender = gender;

            return this;
        }

        /**
         * <p>
         * Assign club property.
         * </p>
         * 
         * @param value A property to assign.
         * @return Chainable API.
         */
        @Override
        public Melty club(Club value) {
            def.club = value;

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
