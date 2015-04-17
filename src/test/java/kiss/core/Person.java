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

/**
 * @version 2015/04/17 23:34:25
 */
public class Person {

    public final String name;

    /**
     * @param name
     */
    public Person(String name) {
        this.name = name;
    }

    public static final Lens<Person, String> $name$ = new Lens<Person, String>() {

        /**
         * {@inheritDoc}
         */
        @Override
        public String get(Person model) {
            return model.name;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Person set(Person model, String property) {
            return new Person(property);
        }
    };
}
