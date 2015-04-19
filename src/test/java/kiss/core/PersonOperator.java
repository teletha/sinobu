/*
 * Copyright (C) 2015 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import java.util.Optional;

import kiss.I;

/**
 * 
 */
public class PersonOperator<T extends PersonOperator, M extends Person> implements Operator<M> {

    public static final Lens<Person, String> _name_ = new Lens<Person, String>() {

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<String> get(Person model) {
            return Optional.of(model.name);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Person set(Person model, String property) {
            return ((PersonOperator) I.find(Operator.class, model.getClass())).copy(model).name(property).build();
        }
    };

    public static final Lens<Person, Integer> _age_ = new Lens<Person, Integer>() {

        /**
         * {@inheritDoc}
         */
        @Override
        public Optional<Integer> get(Person model) {
            return Optional.of(model.age);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Person set(Person model, Integer property) {
            return ((PersonOperator) I.find(Operator.class, model.getClass())).copy(model).age(property).build();
        }
    };

    public static Setter _age_(int property) {
        return null;
    }

    protected String name;

    protected int age;

    protected T copy(M base) {
        this.name = base.name;
        this.age = base.age;

        return (T) this;
    }

    public T name(String name) {
        this.name = name;
        return (T) this;
    }

    public T age(int age) {
        this.age = age;
        return (T) this;
    }

    public Person build() {
        return new Person(name, age);
    }
}