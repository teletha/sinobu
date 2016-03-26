/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.json;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import org.junit.Test;

import kiss.I;
import kiss.model.Model;
import kiss.model.Property;
import kiss.sample.bean.BuiltinBean;
import kiss.sample.bean.ChainBean;
import kiss.sample.bean.DefaultValue;
import kiss.sample.bean.FxPropertyAtField;
import kiss.sample.bean.NestingList;
import kiss.sample.bean.Person;
import kiss.sample.bean.Primitive;
import kiss.sample.bean.School;
import kiss.sample.bean.StringListProperty;
import kiss.sample.bean.StringMapProperty;
import kiss.sample.bean.Student;
import kiss.sample.bean.TransientBean;

/**
 * @version 2016/03/17 1:52:38
 */
public class JSONTest {

    @Test
    public void empty() {
        BuiltinBean instance = new BuiltinBean();

        validate(instance,
        /**/"{}"
        /**/);
    }

    @Test
    public void singleProperty() {
        Person person = new Person();
        person.setAge(20);

        validate(person,
        /**/"{",
        /**/"  'age': '20'",
        /**/"}");
    }

    @Test
    public void multipleProperties() {
        Person person = new Person();
        person.setAge(20);
        person.setFirstName("Umi");
        person.setLastName("Sonoda");

        validate(person,
        /**/"{",
        /**/"  'age': '20',",
        /**/"  'firstName': 'Umi',",
        /**/"  'lastName': 'Sonoda'",
        /**/"}");
    }

    @Test
    public void transientProperty() {
        TransientBean bean = I.make(TransientBean.class);
        bean.setBoth(8);
        bean.setNone(15);

        validate(bean,
        /**/"{",
        /**/"  'none': '15'",
        /**/"}");
    }

    @Test
    public void javaFXProperty() {
        FxPropertyAtField bean = I.make(FxPropertyAtField.class);
        bean.string.set("value");
        bean.integer.set(10);
        bean.list.add("first");
        bean.list.add("second");
        bean.map.put("one", 11L);
        bean.map.put("two", 222L);

        validate(bean,
        /**/"{",
        /**/"  'integer': '10',",
        /**/"  'string': 'value',",
        /**/"  'list': {",
        /**/"    '0': 'first',",
        /**/"    '1': 'second'",
        /**/"  },",
        /**/"  'map': {",
        /**/"    'one': '11',",
        /**/"    'two': '222'",
        /**/"  }",
        /**/"}");
    }

    @Test
    public void defaultValue() {
        DefaultValue instant = new DefaultValue();

        validate(instant,
        /**/"{",
        /**/"  'value': 'default'",
        /**/"}");

        // clear value
        instant.value = null;
        validate(instant,
        /**/"{}"
        /**/);
    }

    @Test
    public void list() {
        List<String> list = new ArrayList();
        list.add("one");
        list.add("two");
        list.add("three");
        StringListProperty strings = I.make(StringListProperty.class);
        strings.setList(list);

        validate(strings,
        /**/"{",
        /**/"  'list': [",
        /**/"    'one',",
        /**/"    'two',",
        /**/"    'three'",
        /**/"  ]",
        /**/"}");
    }

    @Test
    public void listNull() {
        List<String> list = new ArrayList();
        list.add(null);
        list.add("null");
        list.add(null);
        StringListProperty strings = I.make(StringListProperty.class);
        strings.setList(list);

        validate(strings,
        /**/"{",
        /**/"  'list': [",
        /**/"    null,",
        /**/"    'null',",
        /**/"    null",
        /**/"  ]",
        /**/"}");
    }

    @Test
    public void listNested() {
        NestingList list = I.make(NestingList.class);
        list.setNesting(Arrays.asList(Collections.EMPTY_LIST, Collections.EMPTY_LIST));

        validate(list,
        /**/"{",
        /**/"  'nesting': [",
        /**/"    [],",
        /**/"    []",
        /**/"  ]",
        /**/"}");
    }

    @Test
    public void map() {
        Map<String, String> map = new LinkedHashMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");
        StringMapProperty strings = I.make(StringMapProperty.class);
        strings.setMap(map);

        validate(strings,
        /**/"{",
        /**/"  'map': {",
        /**/"    'one': '1',",
        /**/"    'two': '2',",
        /**/"    'three': '3'",
        /**/"  }",
        /**/"}");
    }

    @Test
    public void mapNull() {
        Map<String, String> map = new LinkedHashMap();
        map.put(null, null);
        map.put("null", "NULL");
        StringMapProperty strings = I.make(StringMapProperty.class);
        strings.setMap(map);

        validate(strings,
        /**/"{",
        /**/"  'map': {",
        /**/"    'null': 'NULL'",
        /**/"  }",
        /**/"}");
    }

    @Test
    public void primitives() {
        Primitive primitive = I.make(Primitive.class);
        primitive.setBoolean(true);
        primitive.setChar('c');
        primitive.setInt(-5);
        primitive.setFloat(0.1f);

        validate(primitive,
        /**/"{",
        /**/"  'boolean': 'true',",
        /**/"  'byte': '0',",
        /**/"  'char': 'c',",
        /**/"  'double': '0.0',",
        /**/"  'float': '0.1',",
        /**/"  'int': '-5',",
        /**/"  'long': '0',",
        /**/"  'short': '0'",
        /**/"}");
    }

    @Test
    public void escaped() {
        Person person = new Person();
        person.setAge(20);
        person.setFirstName("A\r\nA\t");
        person.setLastName("B\n\"\\B");

        validate(person,
        /**/"{",
        /**/"  'age': '20',",
        /**/"  'firstName': 'A\\r\\nA\\t',",
        /**/"  'lastName': 'B\\n\\\"\\\\B'",
        /**/"}");
    }

    @Test
    public void codecValue() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setSomeClass(String.class);
        bean.setDate(new Date(0));
        bean.setBigInteger(new BigInteger("1234567890987654321"));

        validate(bean,
        /**/"{",
        /**/"  'bigInteger': '1234567890987654321',",
        /**/"  'date': '1970-01-01T09:00:00',",
        /**/"  'someClass': 'java.lang.String'",
        /**/"}");
    }

    @Test
    public void nestedObject() {
        School school = I.make(School.class);
        school.setName("Sakura High School");

        Student student = I.make(Student.class);
        student.setAge(15);
        student.setFirstName("Mio");
        student.setLastName("Akiyama");
        student.setSchool(school);

        validate(student,
        /**/"{",
        /**/"  'age': '15',",
        /**/"  'firstName': 'Mio',",
        /**/"  'lastName': 'Akiyama',",
        /**/"  'school': {",
        /**/"    'name': 'Sakura High School',",
        /**/"    'students': []",
        /**/"  }",
        /**/"}");
    }

    public void readIncompatible() {
        Person instance = I.read("15", I.make(Person.class));
        assert instance != null;
        assert instance.getAge() == 0;
        assert instance.getFirstName() == null;
        assert instance.getLastName() == null;
    }

    @Test(expected = ClassCircularityError.class)
    public void cyclic() {
        ChainBean chain = I.make(ChainBean.class);
        chain.setNext(chain);

        validate(chain);
    }

    /**
     * <p>
     * Write JSON.
     * </p>
     * 
     * @param texts
     * @return
     */
    private static <M> void validate(M object, String... texts) {
        StringBuilder output = new StringBuilder();
        I.write(object, output);
        String serialized = output.toString();

        StringJoiner joiner = new StringJoiner("\r\n");
        for (String text : texts) {
            text = text.replaceAll("'", "\"");
            joiner.add(text.replaceAll("  ", "\t"));
        }

        // validate serialized text
        assert joiner.toString().equals(serialized);

        // validate model and properties
        Model model = Model.of(object.getClass());

        // write and read
        validate(model, object, I.read(serialized, I.make((Class<M>) model.type)));
    }

    /**
     * <p>
     * Validate object by model.
     * </p>
     * 
     * @param model
     * @param one
     * @param other
     */
    private static void validate(Model model, Object one, Object other) {
        for (Property property : model.properties) {
            Object oneValue = model.get(one, property);
            Object otherValue = model.get(other, property);

            if (property.isTransient) {
                // ignore
            } else if (property.isAttribute()) {
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
