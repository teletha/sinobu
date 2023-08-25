/*
 * Copyright (C) 2023 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.model;

import java.util.List;

import org.junit.jupiter.api.Test;

import kiss.Model;

class SpecializedModelTest {

    static class Person<X> {
        public X name;
    }

    static class Root {
        public Person<String> person;
    }

    @Test
    void specializedType() {
        Model root = Model.of(Root.class);
        assert root.type == Root.class;

        Model person = root.property("person").model;
        assert person.type == Person.class;

        Model name = person.property("name").model;
        assert name.type == String.class;
    }

    @Test
    void specializedList() {
        Model root = Model.of(ListRoot.class);
        assert root.type == ListRoot.class;

        Model list = root.property("list").model;
        assert list.type == List.class;

        Model person = list.property("0").model;
        assert person.type == Person.class;

        Model name = person.property("name").model;
        assert name.type == Integer.class;

    }

    static class ListRoot {
        public List<Person<Integer>> list;
    }

    @Test
    void specializedGenericList() {
        Model personalized = Model.of(PersonalizedRoot.class);
        assert personalized.type == PersonalizedRoot.class;

        Model generic = personalized.property("persons").model;
        assert generic.type == GenericListRoot.class;

        Model list = generic.property("list").model;
        assert list.type == List.class;

        Model person = list.property("0").model;
        assert person.type == Person.class;

        Model name = person.property("name").model;
        assert name.type == Long.class;
    }

    static class GenericListRoot<T> {
        public List<T> list;
    }

    static class PersonalizedRoot {
        public GenericListRoot<Person<Long>> persons;
    }
}
