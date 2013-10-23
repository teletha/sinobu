/*
 * Copyright (C) 2013 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.serialization;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kiss.I;
import kiss.sample.bean.BuiltinBean;
import kiss.sample.bean.ChainBean;
import kiss.sample.bean.Group;
import kiss.sample.bean.Person;
import kiss.sample.bean.Primitive;
import kiss.sample.bean.School;
import kiss.sample.bean.StringListProperty;
import kiss.sample.bean.Student;
import kiss.sample.bean.TransientBean;

import org.junit.Test;

/**
 * @version 2011/03/31 16:59:01
 */
public class JSONTest {

    @Test
    public void escaped() {
        Student student = I.make(Student.class);
        student.setAge(15);
        student.setFirstName("A\r\nA\t");
        student.setLastName("B\n\"B");

        // write
        String json = json(student);
        assert json.equals("{\"age\":\"15\",\"firstName\":\"A\\r\\nA\\t\",\"lastName\":\"B\\n\\\"B\"}");

        // read
        student = I.read(json, I.make(Student.class));
        assert student.getAge() == 15;
        assert student.getFirstName().equals("A\r\nA\t");
        assert student.getLastName().equals("B\n\"B");
        assert student.getSchool() == null;
    }

    @Test
    public void singleProperty() {
        Student student = I.make(Student.class);
        student.setAge(15);

        // write
        String json = json(student);
        assert json.equals("{\"age\":\"15\"}");

        // read
        student = I.read(json, I.make(Student.class));
        assert student.getAge() == 15;
        assert student.getFirstName() == null;
        assert student.getLastName() == null;
    }

    @Test
    public void multipleProperties() {
        Student student = I.make(Student.class);
        student.setAge(15);
        student.setFirstName("Mio");
        student.setLastName("Akiyama");

        // write
        String json = json(student);
        assert json.equals("{\"age\":\"15\",\"firstName\":\"Mio\",\"lastName\":\"Akiyama\"}");

        // read
        student = I.read(json, I.make(Student.class));
        assert student.getAge() == 15;
        assert student.getFirstName().equals("Mio");
        assert student.getLastName().equals("Akiyama");
        assert student.getSchool() == null;
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

        // write
        String json = json(student);
        assert json.equals("{\"age\":\"15\",\"firstName\":\"Mio\",\"lastName\":\"Akiyama\",\"school\":{\"name\":\"Sakura High School\",\"students\":[]}}");

        // read
        student = I.read(json, I.make(Student.class));
        assert student.getAge() == 15;
        assert student.getFirstName().equals("Mio");
        assert student.getLastName().equals("Akiyama");
        assert student.getSchool().getName().equals("Sakura High School");
    }

    @Test
    public void primitives() {
        Primitive primitive = I.make(Primitive.class);
        primitive.setBoolean(true);
        primitive.setChar('c');
        primitive.setBoolean(false);
        primitive.setInt(-5);
        primitive.setFloat(0.1f);

        // write
        String json = json(primitive);
        assert json.equals("{\"boolean\":\"false\",\"byte\":\"0\",\"char\":\"c\",\"double\":\"0.0\",\"float\":\"0.1\",\"int\":\"-5\",\"long\":\"0\",\"short\":\"0\"}");

        // read
        primitive = I.read(json, I.make(Primitive.class));
        assert !primitive.isBoolean();
        assert primitive.getChar() == 'c';
        assert primitive.getInt() == -5;
        assert primitive.getFloat() == 0.1f;
    }

    @Test
    public void list() {
        List<String> list = new ArrayList();
        list.add("one");
        list.add("two");
        list.add("three");

        StringListProperty strings = I.make(StringListProperty.class);
        strings.setList(list);

        // write
        String json = json(strings);
        assert json.equals("{\"list\":[\"one\",\"two\",\"three\"]}");

        // read
        strings = I.read(json, I.make(StringListProperty.class));
        list = strings.getList();
        assert list != null;
        assert list.get(0).equals("one");
        assert list.get(1).equals("two");
        assert list.get(2).equals("three");
    }

    // @Test
    // public void map() {
    // Map<String, String> map = new HashMap();
    // map.put("one", "1");
    // map.put("two", "2");
    // map.put("three", "3");
    //
    // StringMapProperty strings = I.make(StringMapProperty.class);
    // strings.setMap(map);
    //
    // // write
    // String json = json(strings);
    // assert json.equals("{\"map\":{\"two\":\"2\",\"one\":\"1\",\"three\":\"3\"}}");
    //
    // // read
    // strings = I.read(json, I.make(StringMapProperty.class));
    // map = strings.getMap();
    // assert map != null;
    // assert map.get("one").equals("1");
    // }

    @Test
    public void testAttribute() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setSomeClass(String.class);
        bean.setDate(new Date(0));
        bean.setBigInteger(new BigInteger("1234567890987654321"));

        // write
        String json = json(bean);
        assert json.equals("{\"bigInteger\":\"1234567890987654321\",\"date\":\"1970-01-01T09:00:00\",\"someClass\":\"java.lang.String\"}");

        // read
        bean = I.read(json, I.make(BuiltinBean.class));
        assert bean.getSomeClass().equals(String.class);
        assert bean.getDate().equals(new Date(0));
        assert bean.getBigInteger().equals(new BigInteger("1234567890987654321"));
    }

    @Test
    public void duplication() {
        Group group = I.make(Group.class);
        List<Person> list = new ArrayList();
        Person person1 = I.make(Person.class);
        person1.setAge(1);
        Person person2 = I.make(Person.class);
        person2.setAge(2);

        list.add(person1);
        list.add(person1);
        list.add(person2);
        group.setMembers(list);

        // write
        String json = json(group);
        assert json.equals("{\"members\":[{\"age\":\"1\"},{\"age\":\"1\"},{\"age\":\"2\"}]}");
    }

    @Test
    public void testEscapeDoubleQuote() {
        Person person = I.make(Person.class);
        person.setFirstName("\"");

        // write
        String json = json(person);
        assert json.equals("{\"age\":\"0\",\"firstName\":\"\\\"\"}");

        // read
        person = I.read(json, I.make(Person.class));
        assert person.getFirstName().equals("\"");
    }

    @Test
    public void testEscapeBackSlash() {
        Person person = I.make(Person.class);
        person.setFirstName("\\");

        // write
        String json = json(person);
        assert json.equals("{\"age\":\"0\",\"firstName\":\"\\\\\"}");

        // read
        person = I.read(json, I.make(Person.class));
        assert person.getFirstName().equals("\\");
    }

    @Test
    public void readIncompatibleJSON() {
        assert I.read("15", I.make(Person.class)) != null;
    }

    @Test(expected = ClassCircularityError.class)
    public void testCyclicRootNode() {
        ChainBean chain = I.make(ChainBean.class);
        chain.setNext(chain);

        assert json(chain).equals("{\"next\":{}}");
    }

    @Test
    public void transientProperty() {
        TransientBean bean = I.make(TransientBean.class);
        bean.setBoth(8);
        bean.setNone(15);

        // write
        String json = json(bean);
        assert json.equals("{\"none\":\"15\"}");

        // read
        bean = I.read(json, I.make(TransientBean.class));
        assert bean.getNone() == 15;
        assert bean.getBoth() == 0;
    }

    @Test
    public void fromReader() throws Exception {
        Person person = I.read(new StringReader("{\"age\":\"15\",\"firstName\":\"Mio\",\"lastName\":\"Akiyama\"}"), I.make(Person.class));
        assert person.getAge() == 15;
        assert person.getFirstName().equals("Mio");
        assert person.getLastName().equals("Akiyama");
    }

    /**
     * <p>
     * Helper method to convert bean to json expression.
     * </p>
     * 
     * @param model A target bean.
     * @return JSON expression.
     */
    private static String json(Object model) {
        StringBuilder output = new StringBuilder();

        // jsonize
        I.write(model, output, true);

        // API definition
        return output.toString();
    }
}
