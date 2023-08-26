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

import java.time.LocalDate;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.junit.jupiter.api.Test;

import kiss.Model;

class SpecializedModelTest {

    static class Person<X> {
        public X name;
    }

    static class GenericList<T> {
        public List<T> list;
    }

    static class GenericMap<K, V> {
        public Map<K, V> map;
    }

    static class GenericStringKeyMap<V> {
        public Map<String, V> map;
    }

    static class GenericStringValueMap<K> {
        public Map<K, Person<LocalDate>> map;
    }

    @Test
    void specializedType() {
        class Root {
            @SuppressWarnings("unused")
            public Person<String> person;
        }

        Model root = Model.of(Root.class);
        assert root.type == Root.class;

        Model person = root.property("person").model;
        assert person.type == Person.class;

        Model name = person.property("name").model;
        assert name.type == String.class;
    }

    @Test
    void specializedList() {
        class Root {
            @SuppressWarnings("unused")
            public List<Person<Integer>> list;
        }

        Model root = Model.of(Root.class);
        assert root.type == Root.class;

        Model list = root.property("list").model;
        assert list.type == List.class;

        Model person = list.property("0").model;
        assert person.type == Person.class;

        Model name = person.property("name").model;
        assert name.type == Integer.class;

    }

    @Test
    void specializedGenericList() {
        class Root {
            @SuppressWarnings("unused")
            public GenericList<Person<Long>> persons;
        }

        Model root = Model.of(Root.class);
        assert root.type == Root.class;

        Model generic = root.property("persons").model;
        assert generic.type == GenericList.class;

        Model list = generic.property("list").model;
        assert list.type == List.class;

        Model person = list.property("0").model;
        assert person.type == Person.class;

        Model name = person.property("name").model;
        assert name.type == Long.class;
    }

    @Test
    void specializedGenericMap() {
        class Root {
            @SuppressWarnings("unused")
            public GenericMap<String, Person<Locale>> persons;
        }

        Model root = Model.of(Root.class);
        assert root.type == Root.class;

        Model generic = root.property("persons").model;
        assert generic.type == GenericMap.class;

        Model list = generic.property("map").model;
        assert list.type == Map.class;

        Model person = list.property("key").model;
        assert person.type == Person.class;

        Model name = person.property("name").model;
        assert name.type == Locale.class;
    }

    @Test
    void specializedKeyMap() {
        class Root {
            @SuppressWarnings("unused")
            public GenericStringKeyMap<Person<Currency>> persons;
        }

        Model root = Model.of(Root.class);
        assert root.type == Root.class;

        Model generic = root.property("persons").model;
        assert generic.type == GenericStringKeyMap.class;

        Model map = generic.property("map").model;
        assert map.type == Map.class;

        Model person = map.property("key").model;
        assert person.type == Person.class;

        Model name = person.property("name").model;
        assert name.type == Currency.class;
    }

    @Test
    void specializedValueMap() {
        class Root {
            @SuppressWarnings("unused")
            public GenericStringValueMap<String> persons;
        }

        Model root = Model.of(Root.class);
        assert root.type == Root.class;

        Model generic = root.property("persons").model;
        assert generic.type == GenericStringValueMap.class;

        Model map = generic.property("map").model;
        assert map.type == Map.class;

        Model person = map.property("key").model;
        assert person.type == Person.class;

        Model name = person.property("name").model;
        assert name.type == LocalDate.class;
    }
}
