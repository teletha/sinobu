/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.json;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.JSON;
import kiss.Model;
import kiss.Property;
import kiss.sample.bean.BuiltinBean;
import kiss.sample.bean.ChainBean;
import kiss.sample.bean.NestingList;
import kiss.sample.bean.Person;
import kiss.sample.bean.Primitive;
import kiss.sample.bean.School;
import kiss.sample.bean.StringListProperty;
import kiss.sample.bean.StringMapProperty;
import kiss.sample.bean.Student;
import kiss.sample.bean.TransientBean;

class JSONTest {

    @Test
    void empty() {
        BuiltinBean instance = new BuiltinBean();

        // @formatter:off
        validate(instance,
        "{",
        "  'bigDecimal': null,",
        "  'bigInteger': null,",
        "  'file': null,",
        "  'schoolEnum': null,",
        "  'someClass': null,",
        "  'stringBuffer': null,",
        "  'stringBuilder': null",
        "}");
        // @formatter:on
    }

    @Test
    void singleProperty() {
        Person person = new Person();
        person.setAge(20);

        validate(person, """
                {
                  "age": 20,
                  "firstName": null,
                  "lastName": null
                }""");
    }

    @Test
    void multipleProperties() {
        Person person = new Person();
        person.setAge(20);
        person.setFirstName("Umi");
        person.setLastName("Sonoda");

        // @formatter:off
        validate(person,
        "{",
        "  'age': 20,",
        "  'firstName': 'Umi',",
        "  'lastName': 'Sonoda'",
        "}");
        // @formatter:on
    }

    @Test
    void transientProperty() {
        TransientBean bean = I.make(TransientBean.class);
        bean.field = "transient";
        bean.noneField = "serializable";
        bean.variable.set("transient");
        bean.noneVariable.set("serializable");

        // @formatter:off
        validate(bean,
        "{",
        "  'noneField': 'serializable',",
        "  'noneVariable': 'serializable'",
        "}");
        // @formatter:on
    }

    @SuppressWarnings("unused")
    @Test
    void defaultValue() {
        class PropertyWithDefaultValue {

            public String value = "default";

            public List<String> items = new ArrayList(Collections.singleton("default"));
        }

        PropertyWithDefaultValue instant = new PropertyWithDefaultValue();

        // @formatter:off
        validate(instant,
        "{",
        "  'items': [",
        "    'default'",
        "  ],",
        "  'value': 'default'",
        "}");
         // @formatter:on

        // clear value
        instant.value = null;
        instant.items = null;

        // @formatter:off
        validate(instant,
        "{",
        "  'items': null,",
        "  'value': null",
        "}");
        // @formatter:on
    }

    @Test
    void list() {
        List<String> list = new ArrayList();
        list.add("one");
        list.add("two");
        list.add("three");
        StringListProperty strings = I.make(StringListProperty.class);
        strings.setList(list);

        // @formatter:off
        validate(strings,
        "{",
        "  'list': [",
        "    'one',",
        "    'two',",
        "    'three'",
        "  ]",
        "}");
        // @formatter:on
    }

    @Test
    void listObject() {
        class Persons {
            public List<Person> list = new ArrayList();
        }

        Persons persons = new Persons();
        persons.list.add(person("Ami", 22));
        persons.list.add(person("Kei", 24));

        validate(persons, """
                {
                  'list': [
                    {
                      'age': 22,
                      'firstName': 'Ami',
                      'lastName': null
                    },
                    {
                      'age': 24,
                      'firstName': 'Kei',
                      'lastName': null
                    }
                  ]
                }""");
    }

    private Person person(String name, int age) {
        Person p = new Person();
        p.setFirstName(name);
        p.setAge(age);

        return p;
    }

    @Test
    void listNull() {
        List<String> list = new ArrayList();
        list.add(null);
        list.add("null");
        list.add(null);
        StringListProperty strings = I.make(StringListProperty.class);
        strings.setList(list);

        // @formatter:off
        validate(strings,
        "{",
        "  'list': [",
        "    null,",
        "    'null',",
        "    null",
        "  ]",
        "}");
        // @formatter:on
    }

    @Test
    void listNested() {
        NestingList list = I.make(NestingList.class);
        list.setNesting(Arrays.asList(Collections.EMPTY_LIST, Collections.EMPTY_LIST));

        // @formatter:off
        validate(list,
        "{",
        "  'nesting': [",
        "    [],",
        "    []",
        "  ]",
        "}");
        // @formatter:on
    }

    @Test
    void map() {
        Map<String, String> map = new LinkedHashMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");
        StringMapProperty strings = I.make(StringMapProperty.class);
        strings.setMap(map);

        // @formatter:off
        validate(strings,
        "{",
        "  'map': {",
        "    'one': '1',",
        "    'two': '2',",
        "    'three': '3'",
        "  }",
        "}");
        // @formatter:on
    }

    @Test
    void mapNull() {
        Map<String, String> map = new LinkedHashMap();
        map.put(null, null);
        map.put("null", "NULL");
        StringMapProperty strings = I.make(StringMapProperty.class);
        strings.setMap(map);

        // @formatter:off
        validate(strings,
        "{",
        "  'map': {",
        "    'null': 'NULL'",
        "  }",
        "}");
        // @formatter:on
    }

    @Test
    void primitives() {
        Primitive primitive = I.make(Primitive.class);
        primitive.setBoolean(true);
        primitive.setChar('c');
        primitive.setInt(-5);
        primitive.setFloat(0.1f);

        // @formatter:off
        validate(primitive,
        "{",
        "  'boolean': true,",
        "  'byte': 0,",
        "  'char': 'c',",
        "  'double': 0.0,",
        "  'float': 0.1,",
        "  'int': -5,",
        "  'long': 0,",
        "  'short': 0",
        "}");
        // @formatter:on
    }

    @Test
    void escaped() {
        Person person = new Person();
        person.setAge(20);
        person.setFirstName("A\r\nA\t");
        person.setLastName("B\n\"\\B");

        // @formatter:off
        validate(person,
        "{",
        "  'age': 20,",
        "  'firstName': 'A\\r\\nA\\t',",
        "  'lastName': 'B\\n\\\"\\\\B'",
        "}");
        // @formatter:on
    }

    @Test
    void codecValue() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setSomeClass(String.class);
        bean.setBigInteger(new BigInteger("1234567890987654321"));

        // @formatter:off
        validate(bean,
        "{",
        "  'bigDecimal': null,",
        "  'bigInteger': '1234567890987654321',",
        "  'file': null,",
        "  'schoolEnum': null,",
        "  'someClass': 'java.lang.String',",
        "  'stringBuffer': null,",
        "  'stringBuilder': null",
        "}");
        // @formatter:on
    }

    @Test
    void nestedObject() {
        School school = I.make(School.class);
        school.setName("Sakura High School");

        Student student = I.make(Student.class);
        student.setAge(15);
        student.setFirstName("Mio");
        student.setLastName("Akiyama");
        student.setSchool(school);

        // @formatter:off
        validate(student,
        "{",
        "  'age': 15,",
        "  'firstName': 'Mio',",
        "  'lastName': 'Akiyama',",
        "  'school': {",
        "    'name': 'Sakura High School',",
        "    'students': [],",
        "    'teachers': null",
        "  }",
        "}");
        // @formatter:on
    }

    @Test
    void cyclic() {
        assertThrows(ClassCircularityError.class, () -> {
            ChainBean chain = I.make(ChainBean.class);
            chain.setNext(chain);
            validate(chain);
        });
    }

    @Test
    void getString() {
        Person person = new Person();
        person.setFirstName("name");

        JSON json = I.json(I.write(person));
        assert json.get("firstName").as(String.class).equals("name");
    }

    @Test
    void getInteger() {
        Person person = new Person();
        person.setAge(15);

        JSON json = I.json(I.write(person));
        assert json.get("age").as(int.class) == 15;
    }

    @Test
    void storeImplementation() {
        Dog dog = new Dog();
        dog.name = "pochi";
        dog.size = 3;

        Cat cat = new Cat();
        cat.name = "mike";
        cat.weight = 4;

        Animals animals = new Animals();
        animals.member.add(dog);
        animals.member.add(cat);

        // @formatter:off
        validate(animals,
        "{",
        "  'member': [",
        "    {",
        "      '#': 'kiss.json.JSONTest$Dog',",
        "      'name': 'pochi',",
        "      'size': 3",
        "    },",
        "    {",
        "      '#': 'kiss.json.JSONTest$Cat',",
        "      'name': 'mike',",
        "      'weight': 4",
        "    }",
        "  ]",
        "}");
        // @formatter:on
    }

    @Test
    void storeImplementationForNull() {
        AnimalRef animals = new AnimalRef();

        // @formatter:off
        validate(animals,
        "{",
        "  'animal': null",
        "}");
        // @formatter:on
    }

    @Test
    void recordRoot() {
        record Person(String name, int age) {
        }

        Person person = new Person("Misa", 28);
        validate(person, """
                {
                  "age": 28,
                  "name": "Misa"
                }""");
    }

    @Test
    void recordInList() {
        record Person(String name, int age) {
        }

        @SuppressWarnings("serial")
        class Members extends ArrayList<Person> {
        }

        Members members = new Members();
        members.add(new Person("Misa", 28));
        members.add(new Person("Ayu", 27));

        validate(members, """
                [
                  {
                    "age": 28,
                    "name": "Misa"
                  },
                  {
                    "age": 27,
                    "name": "Ayu"
                  }
                ]""");
    }

    @Test
    void recordWithList() {
        record Person(String name, List<String> nicks) {
        }

        Person person = new Person("Misa", List.of("Micha", "Mipi"));

        validate(person, """
                {
                  "name": "Misa",
                  "nicks": [
                    "Micha",
                    "Mipi"
                  ]
                }""");
    }

    static class AnimalRef {
        public Animal animal;
    }

    private static class Animals {
        public List<Animal> member = new ArrayList();
    }

    static abstract class Animal {
        public String name;
    }

    static class Dog extends Animal {
        public int size;
    }

    static class Cat extends Animal {
        public int weight;
    }

    /**
     * Write JSON.
     * 
     * @param texts
     */
    private static <M> void validate(M object, String... texts) {
        StringBuilder output = new StringBuilder();
        I.write(object, output);
        String serialized = output.toString();

        StringJoiner joiner = new StringJoiner("\n");
        for (String text : texts) {
            text = text.replaceAll("'", "\"");
            joiner.add(text.replaceAll("  ", "\t"));
        }

        // validate serialized text
        assert joiner.toString().equals(serialized);

        // validate model and properties
        Model model = Model.of(object.getClass());

        // write and read
        validate(model, object, I.json(serialized).as(model.type));
        validate(model, object, I.json(serialized, model.type));
    }

    /**
     * Validate object by model.
     * 
     * @param model
     * @param one
     * @param other
     */
    private static void validate(Model<Object> model, Object one, Object other) {
        List<Property> properties = new ArrayList();
        model.walk(one, (m, p, o) -> properties.add(p));

        for (Property property : properties) {
            Object oneValue = model.get(one, property);
            Object otherValue = model.get(other, property);

            if (property.transitory) {
                // ignore
            } else if (property.model.atomic) {
                if (oneValue == null) {
                    assert otherValue == null;
                } else {
                    assert oneValue.equals(otherValue);
                }
            } else {
                validate(property.model, oneValue, otherValue);
            }
        }
    }
}