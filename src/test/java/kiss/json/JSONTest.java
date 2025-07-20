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

@SuppressWarnings("serial")
class JSONTest {

    @Test
    void empty() {
        BuiltinBean instance = new BuiltinBean();

        validate(instance, """
                {
                  "bigDecimal": null,
                  "bigInteger": null,
                  "file": null,
                  "schoolEnum": null,
                  "someClass": null,
                  "stringBuffer": null,
                  "stringBuilder": null
                }
                """);
    }

    @Test
    void singleProperty() {
        record A(int num) {
        }

        validate(new A(20), """
                {
                  "num": 20
                }
                """);
    }

    @Test
    void multipleProperties() {
        record A(int num, String name) {
        }

        validate(new A(20, "Umi"), """
                {
                  "name": "Umi",
                  "num": 20
                }
                """);
    }

    @Test
    void transientProperty() {
        TransientBean bean = new TransientBean();
        bean.field = "transient";
        bean.noneField = "serializable";
        bean.variable.set("transient");
        bean.noneVariable.set("serializable");

        validate(bean, """
                {
                  "noneField": "serializable",
                  "noneVariable": "serializable"
                }
                """);
    }

    @Test
    @SuppressWarnings("unused")
    void defaultValue() {
        class PropertyWithDefaultValue {

            public String value = "default";

            public List<String> items = I.list("default");
        }

        PropertyWithDefaultValue instant = new PropertyWithDefaultValue();

        validate(instant, """
                {
                  "items": [
                    "default"
                  ],
                  "value": "default"
                }
                """);

        // clear value
        instant.value = null;
        instant.items = null;

        validate(instant, """
                {
                  "items": null,
                  "value": null
                }
                """);
    }

    @Test
    void listStringProperty() {
        class Some {
            public List<String> list = new ArrayList();
        }

        Some instance = new Some();
        instance.list.addAll(I.list("one", "two", "three"));

        validate(instance, """
                {
                  "list": [
                    "one",
                    "two",
                    "three"
                  ]
                }
                """);
    }

    @Test
    void listIntegerProperty() {
        class Some {
            public List<Integer> list = new ArrayList();
        }

        Some instance = new Some();
        instance.list.addAll(I.list(1, 2, 3));

        validate(instance, """
                {
                  "list": [
                    1,
                    2,
                    3
                  ]
                }
                """);
    }

    @Test
    void listLongProperty() {
        class Some {
            public List<Long> list = new ArrayList();
        }

        Some instance = new Some();
        instance.list.addAll(I.list(1L, 2L));

        validate(instance, """
                {
                  "list": [
                    1,
                    2
                  ]
                }
                """);
    }

    @Test
    void listDoubleProperty() {
        class Some {
            public List<Double> list = new ArrayList();
        }

        Some instance = new Some();
        instance.list.addAll(I.list(1D, 2D));

        validate(instance, """
                {
                  "list": [
                    1.0,
                    2.0
                  ]
                }
                """);
    }

    @Test
    void listShortProperty() {
        class Some {
            public List<Short> list = new ArrayList();
        }

        Some instance = new Some();
        instance.list.addAll(I.list((short) 1, (short) 2));

        validate(instance, """
                {
                  "list": [
                    1,
                    2
                  ]
                }
                """);
    }

    @Test
    void listByteProperty() {
        class Some {
            public List<Byte> list = new ArrayList();
        }

        Some instance = new Some();
        instance.list.addAll(I.list((byte) 1, (byte) 2));

        validate(instance, """
                {
                  "list": [
                    1,
                    2
                  ]
                }
                """);
    }

    @Test
    void listFloatProperty() {
        class Some {
            public List<Float> list = new ArrayList();
        }

        Some instance = new Some();
        instance.list.addAll(I.list(1F, 2F));

        validate(instance, """
                {
                  "list": [
                    1.0,
                    2.0
                  ]
                }
                """);
    }

    @Test
    void listBooleanProperty() {
        class Some {
            public List<Boolean> list = new ArrayList();
        }

        Some instance = new Some();
        instance.list.addAll(I.list(true, false));

        validate(instance, """
                {
                  "list": [
                    true,
                    false
                  ]
                }
                """);
    }

    @Test
    void listObjectProperty() {
        record Person(String name, int age) {
        }

        class Group {
            public List<Person> member = new ArrayList();
        }

        Group group = new Group();
        group.member.add(new Person("Ami", 22));
        group.member.add(new Person("Kei", 24));

        validate(group, """
                {
                  "member": [
                    {
                      "age": 22,
                      "name": "Ami"
                    },
                    {
                      "age": 24,
                      "name": "Kei"
                    }
                  ]
                }
                """);
    }

    @Test
    void listIntegerType() {
        class Some extends ArrayList<Integer> {
        }

        Some instance = new Some();
        instance.addAll((I.list(1, 2)));

        validate(instance, """
                [
                  1,
                  2
                ]
                """);
    }

    @Test
    void listLongType() {
        class Some extends ArrayList<Long> {
        }

        Some instance = new Some();
        instance.addAll((I.list(1L, 2L)));

        validate(instance, """
                [
                  1,
                  2
                ]
                """);
    }

    @Test
    void listFloatType() {
        class Some extends ArrayList<Float> {
        }

        Some instance = new Some();
        instance.addAll((I.list(1F, 2F)));

        validate(instance, """
                [
                  1.0,
                  2.0
                ]
                """);
    }

    @Test
    void listDoubleType() {
        class Some extends ArrayList<Double> {
        }

        Some instance = new Some();
        instance.addAll((I.list(1D, 2D)));

        validate(instance, """
                [
                  1.0,
                  2.0
                ]
                """);
    }

    @Test
    void listShortType() {
        class Some extends ArrayList<Short> {
        }

        Some instance = new Some();
        instance.addAll((I.list((short) 1, (short) 2)));

        validate(instance, """
                [
                  1,
                  2
                ]
                """);
    }

    @Test
    void listByteType() {
        class Some extends ArrayList<Byte> {
        }

        Some instance = new Some();
        instance.addAll((I.list((byte) 1, (byte) 2)));

        validate(instance, """
                [
                  1,
                  2
                ]
                """);
    }

    @Test
    void listBooleanType() {
        class Some extends ArrayList<Boolean> {
        }

        Some instance = new Some();
        instance.addAll((I.list(true, false)));

        validate(instance, """
                [
                  true,
                  false
                ]
                """);
    }

    @Test
    void listObjectType() {
        record Person(String name, int age) {
        }

        class Group extends ArrayList<Person> {
        }

        Group group = new Group();
        group.add(new Person("Ami", 22));
        group.add(new Person("Kei", 24));

        validate(group, """
                [
                  {
                    "age": 22,
                    "name": "Ami"
                  },
                  {
                    "age": 24,
                    "name": "Kei"
                  }
                ]
                """);
    }

    @Test
    void listNull() {
        List<String> list = new ArrayList();
        list.add(null);
        list.add("null");
        list.add(null);
        StringListProperty strings = I.make(StringListProperty.class);
        strings.setList(list);

        validate(strings, """
                {
                  "list": [
                    null,
                    "null",
                    null
                  ]
                }
                """);

    }

    @Test
    void listNested() {
        NestingList list = I.make(NestingList.class);
        list.setNesting(Arrays.asList(Collections.EMPTY_LIST, Collections.EMPTY_LIST));

        validate(list, """
                {
                  "nesting": [
                    [],
                    []
                  ]
                }
                """);

    }

    @Test
    void emptyListAndMap() {
        @SuppressWarnings("unused")
        class Container {
            public List<String> emptyList = new ArrayList<>();

            public Map<String, String> emptyMap = Collections.emptyMap();
        }

        validate(new Container(), """
                {
                  "emptyList": [],
                  "emptyMap": {}
                }
                """);
    }

    @Test
    void map() {
        Map<String, String> map = new LinkedHashMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");
        StringMapProperty strings = I.make(StringMapProperty.class);
        strings.setMap(map);

        validate(strings, """
                {
                  "map": {
                    "one": "1",
                    "two": "2",
                    "three": "3"
                  }
                }
                """);

    }

    @Test
    void mapNull() {
        Map<String, String> map = new LinkedHashMap();
        map.put(null, null);
        map.put("null", "NULL");
        StringMapProperty strings = I.make(StringMapProperty.class);
        strings.setMap(map);

        validate(strings, """
                {
                  "map": {
                    "null": "NULL"
                  }
                }
                """);

    }

    @Test
    void primitives() {
        Primitive primitive = I.make(Primitive.class);
        primitive.setBoolean(true);
        primitive.setChar('c');
        primitive.setInt(-5);
        primitive.setFloat(0.1f);

        validate(primitive, """
                {
                  "boolean": true,
                  "byte": 0,
                  "char": "c",
                  "double": 0.0,
                  "float": 0.1,
                  "int": -5,
                  "long": 0,
                  "short": 0
                }
                """);

    }

    @Test
    void escaped() {
        Person person = new Person();
        person.setAge(20);
        person.setFirstName("A\r\nA\t");
        person.setLastName("B\n\"\\B");

        validate(person, """
                {
                  "age": 20,
                  "firstName": "A\\r\\nA\\t",
                  "lastName": "B\\n\\\"\\\\B"
                }
                """);

    }

    @Test
    void codecValue() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setSomeClass(String.class);
        bean.setBigInteger(new BigInteger("1234567890987654321"));

        validate(bean, """
                {
                  "bigDecimal": null,
                  "bigInteger": "1234567890987654321",
                  "file": null,
                  "schoolEnum": null,
                  "someClass": "java.lang.String",
                  "stringBuffer": null,
                  "stringBuilder": null
                }
                """);

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

        validate(student, """
                {
                  "age": 15,
                  "firstName": "Mio",
                  "lastName": "Akiyama",
                  "school": {
                    "name": "Sakura High School",
                    "students": [],
                    "teachers": null
                  }
                }
                """);

    }

    @Test
    @SuppressWarnings("unused")
    void nestedNulls() {

        class Inner {
            public String name;
        }

        class Outer {
            public Inner inner;
        }

        Outer outer = new Outer();
        outer.inner = null;

        validate(outer, """
                {
                  "inner": null
                }
                """);
    }

    @Test
    void cyclic() {
        assertThrows(ClassCircularityError.class, () -> {
            ChainBean chain = I.make(ChainBean.class);
            chain.setNext(chain);
            validate(chain, "");
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

        validate(animals, """
                {
                  "member": [
                    {
                      "#": "kiss.json.JSONTest$Dog",
                      "name": "pochi",
                      "size": 3
                    },
                    {
                      "#": "kiss.json.JSONTest$Cat",
                      "name": "mike",
                      "weight": 4
                    }
                  ]
                }
                """);

    }

    @Test
    void storeImplementationForNull() {
        AnimalRef animals = new AnimalRef();

        validate(animals, """
                {
                  "animal": null
                }
                """);

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
                }
                """);
    }

    @Test
    void recordInList() {
        record Person(String name, int age) {
        }

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
                ]
                """);
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
                }
                """);
    }

    @Test
    @SuppressWarnings("unused")
    void enumField() {
        enum Status {
            ACTIVE, INACTIVE
        }

        class WithEnum {
            public Status status = Status.ACTIVE;
        }

        validate(new WithEnum(), """
                {
                  "status": "ACTIVE"
                }
                """);
    }

    @Test
    void ignoreUnknownProperty() {
        class Bean {
            public String name;
        }

        Bean bean = I.json("""
                {
                    "name": "test",
                    "unknown": "value"
                }
                """, Bean.class);

        assert bean.name.equals("test");
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
     * Validates that the given object can be correctly serialized to JSON and then
     * deserialized back without losing any information.
     * <p>
     * This method performs a round-trip test to ensure that:
     * <ul>
     * <li>The object can be serialized into the expected JSON string.</li>
     * <li>The JSON can be deserialized back into an equivalent object.</li>
     * <li>The object's internal state remains consistent via model-based property comparisons.</li>
     * </ul>
     *
     * @param object The object to serialize and validate.
     * @param text The expected JSON string representation.
     * @param <M> The type of the object.
     */
    private static <M> void validate(M object, String text) {
        StringBuilder output = new StringBuilder();
        I.write(object, output); // serialize the object
        String serialized = output.toString();

        // Normalize formatting and compare to expected JSON
        text = text.trim();
        serialized = serialized.replaceAll("\t", "  ");
        assert text.equals(serialized);

        // Validate deserialization and internal model consistency
        Model model = Model.of(object.getClass());
        validate(model, object, I.json(serialized).as(model.type)); // implicit type
        validate(model, object, I.json(serialized, model.type)); // explicit type
    }

    /**
     * Recursively compares all properties of two objects based on the given model.
     * <p>
     * This method traverses the model structure to validate that all corresponding
     * properties in the original and deserialized objects are equal. Atomic values are
     * compared via {@code equals()}, and nested models are validated recursively.
     *
     * @param model The model definition that describes the structure of the objects.
     * @param one The original object.
     * @param other The deserialized object.
     */
    @SuppressWarnings("unused")
    private static void validate(Model<Object> model, Object one, Object other) {
        List<Property> properties = new ArrayList<>();
        model.walk(one, (m, p, o) -> properties.add(p)); // collect all properties

        for (Property property : properties) {
            Object oneValue = model.get(one, property);
            Object otherValue = model.get(other, property);

            if (property.transitory) {
                // Skip transient fields (e.g., not serialized)
            } else if (property.model.atomic) {
                // Directly compare atomic values
                if (oneValue == null) {
                    assert otherValue == null;
                } else {
                    assert oneValue.equals(otherValue);
                }
            } else {
                // Recurse for nested models
                validate(property.model, oneValue, otherValue);
            }
        }
    }
}