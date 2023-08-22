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

class NestedGenericModelTest {

    @Test
    void nested1() {
        Model root = Model.of(Root.class);
        assert root.type == Root.class;

        Model list = root.property("list").model;
        assert list.type == List.class;

        Model person = list.property("0").model;
        assert person.type == Person.class;
    }

    static class Person {
        public String name;
    }

    static class Root {
        public List<Person> list;
    }

    @Test
    void nested2() {
        Model personalized = Model.of(PersonalizedRoot.class);
        assert personalized.type == PersonalizedRoot.class;

        Model generic = personalized.property("persons").model;
        assert generic.type == GenericRoot.class;

        Model list = generic.property("list").model;
        assert list.type == List.class;

        Model person = list.property("0").model;
        assert person.type == Person.class;
    }

    static class GenericRoot<T> {
        public List<T> list;
    }

    static class PersonalizedRoot {
        public GenericRoot<Person> persons;
    }
}
