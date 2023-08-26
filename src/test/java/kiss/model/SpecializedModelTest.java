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
import kiss.Variable;

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
        @SuppressWarnings("unused")
        class Root {
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
        @SuppressWarnings("unused")
        class Root {
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
        @SuppressWarnings("unused")
        class Root {
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
        @SuppressWarnings("unused")
        class Root {
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
        @SuppressWarnings("unused")
        class Root {
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
        @SuppressWarnings("unused")
        class Root {
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

    @Test
    void specializedTypeMethod() {
        @SuppressWarnings("unused")
        class Root {
            private Person<String> person;

            public Person<String> getPerson() {
                return person;
            }

            public void setPerson(Person<String> person) {
                this.person = person;
            }
        }

        Model root = Model.of(Root.class);
        assert root.type == Root.class;

        Model person = root.property("person").model;
        assert person.type == Person.class;

        Model name = person.property("name").model;
        assert name.type == String.class;
    }

    @Test
    void specializedListMethod() {
        @SuppressWarnings("unused")
        class Root {
            private List<Person<Integer>> list;

            public final List<Person<Integer>> getList() {
                return list;
            }

            public final void setList(List<Person<Integer>> list) {
                this.list = list;
            }
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
    void specializedGenericListMethod() {
        @SuppressWarnings("unused")
        class Root {
            private GenericList<Person<Long>> persons;

            public final GenericList<Person<Long>> getPersons() {
                return persons;
            }

            public final void setPersons(GenericList<Person<Long>> persons) {
                this.persons = persons;
            }
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
    void specializedGenericMapMethod() {
        @SuppressWarnings("unused")
        class Root {
            private GenericMap<String, Person<Locale>> persons;

            public final GenericMap<String, Person<Locale>> getPersons() {
                return persons;
            }

            public final void setPersons(GenericMap<String, Person<Locale>> persons) {
                this.persons = persons;
            }
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
    void specializedKeyMapMethod() {
        @SuppressWarnings("unused")
        class Root {
            private GenericStringKeyMap<Person<Currency>> persons;

            public final GenericStringKeyMap<Person<Currency>> getPersons() {
                return persons;
            }

            public final void setPersons(GenericStringKeyMap<Person<Currency>> persons) {
                this.persons = persons;
            }
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
    void specializedValueMapMethod() {
        @SuppressWarnings("unused")
        class Root {
            private GenericStringValueMap<String> persons;

            public final GenericStringValueMap<String> getPersons() {
                return persons;
            }

            public final void setPersons(GenericStringValueMap<String> persons) {
                this.persons = persons;
            }
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

    @Test
    void specializedTypeVariable() {
        @SuppressWarnings("unused")
        class Root {
            public Variable<Person<String>> person;
        }

        Model root = Model.of(Root.class);
        assert root.type == Root.class;

        Model person = root.property("person").model;
        assert person.type == Person.class;

        Model name = person.property("name").model;
        assert name.type == String.class;
    }

    @Test
    void specializedListVariable() {
        @SuppressWarnings("unused")
        class Root {
            public Variable<List<Person<Integer>>> list;
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
    void specializedGenericListVariable() {
        @SuppressWarnings("unused")
        class Root {
            public Variable<GenericList<Person<Long>>> persons;
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
    void specializedGenericMapVariable() {
        @SuppressWarnings("unused")
        class Root {
            public Variable<GenericMap<String, Person<Locale>>> persons;
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
    void specializedKeyMapVariable() {
        @SuppressWarnings("unused")
        class Root {
            public Variable<GenericStringKeyMap<Person<Currency>>> persons;
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
    void specializedValueMapVariable() {
        @SuppressWarnings("unused")
        class Root {
            public Variable<GenericStringValueMap<String>> persons;
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
