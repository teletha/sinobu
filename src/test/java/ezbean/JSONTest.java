/*
 * Copyright (C) 2010 Nameless Production Committee.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean;

import static org.junit.Assert.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ezbean.sample.bean.BuiltinBean;
import ezbean.sample.bean.ChainBean;
import ezbean.sample.bean.Group;
import ezbean.sample.bean.Person;
import ezbean.sample.bean.Primitive;
import ezbean.sample.bean.School;
import ezbean.sample.bean.StringList;
import ezbean.sample.bean.StringMap;
import ezbean.sample.bean.Student;

/**
 * @version 2009/04/11 18:50:31
 */
public class JSONTest {

    @Test
    public void testObjectWithSingleProperty() {
        Student student = I.make(Student.class);
        student.setAge(15);

        // write
        String json = I.json(student);
        assertEquals("{\"age\":\"15\"}", json);

        // read
        student = I.json(Student.class, json);
        assertEquals(15, student.getAge());
        assertNull(student.getFirstName());
        assertNull(student.getLastName());
    }

    @Test
    public void testObjectWithMultipleProperties() {
        Student student = I.make(Student.class);
        student.setAge(15);
        student.setFirstName("Mio");
        student.setLastName("Akiyama");

        // write
        String json = I.json(student);
        assertEquals("{\"age\":\"15\",\"firstName\":\"Mio\",\"lastName\":\"Akiyama\"}", json);

        // read
        student = I.json(Student.class, json);
        assertEquals(15, student.getAge());
        assertEquals("Mio", student.getFirstName());
        assertEquals("Akiyama", student.getLastName());
        assertNull(student.getSchool());
    }

    @Test
    public void testNestedObject() {
        School school = I.make(School.class);
        school.setName("Sakura High School");

        Student student = I.make(Student.class);
        student.setAge(15);
        student.setFirstName("Mio");
        student.setLastName("Akiyama");
        student.setSchool(school);

        // write
        String json = I.json(student);
        assertEquals("{\"age\":\"15\",\"firstName\":\"Mio\",\"lastName\":\"Akiyama\",\"school\":{\"name\":\"Sakura High School\"}}", json);

        // read
        student = I.json(Student.class, json);
        assertEquals(15, student.getAge());
        assertEquals("Mio", student.getFirstName());
        assertEquals("Akiyama", student.getLastName());
        assertEquals("Sakura High School", student.getSchool().getName());
    }

    @Test
    public void testPrimitives() {
        Primitive primitive = I.make(Primitive.class);
        primitive.setBoolean(true);
        primitive.setChar('c');
        primitive.setBoolean(false);
        primitive.setInt(-5);
        primitive.setFloat(0.1f);

        // write
        String json = I.json(primitive);
        assertEquals("{\"boolean\":\"false\",\"byte\":\"0\",\"char\":\"c\",\"double\":\"0.0\",\"float\":\"0.1\",\"int\":\"-5\",\"long\":\"0\",\"short\":\"0\"}", json);

        // read
        primitive = I.json(Primitive.class, json);
        assertFalse(primitive.isBoolean());
        assertEquals('c', primitive.getChar());
        assertEquals(-5, primitive.getInt());
        assertEquals(0.1f, primitive.getFloat(), 0d);
    }

    @Test
    public void list() {
        List<String> list = new ArrayList();
        list.add("one");
        list.add("two");
        list.add("three");

        StringList strings = I.make(StringList.class);
        strings.setList(list);

        // write
        String json = I.json(strings);
        assertEquals("{\"list\":[\"one\",\"two\",\"three\"]}", json);

        // read
        strings = I.json(StringList.class, json);
        list = strings.getList();
        assertNotNull(list);
        assertEquals("one", list.get(0));
        assertEquals("two", list.get(1));
        assertEquals("three", list.get(2));
    }

    @Test
    public void map() {
        Map<String, String> map = new HashMap();
        map.put("one", "1");
        map.put("two", "2");
        map.put("three", "3");

        StringMap strings = I.make(StringMap.class);
        strings.setMap(map);

        // write
        String json = I.json(strings);
        assertEquals("{\"map\":{\"two\":\"2\",\"one\":\"1\",\"three\":\"3\"}}", json);

        // read
        strings = I.json(StringMap.class, json);
        map = strings.getMap();
        assertNotNull(map);
        assertEquals("1", map.get("one"));
    }

    @Test
    public void testAttribute() {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setSomeClass(String.class);
        bean.setDate(new Date(0));
        bean.setBigInteger(new BigInteger("1234567890987654321"));

        // write
        String json = I.json(bean);
        assertEquals("{\"bigInteger\":\"1234567890987654321\",\"date\":\"1970-01-01T09:00:00\",\"someClass\":\"java.lang.String\"}", json);

        // read
        bean = I.json(BuiltinBean.class, json);
        assertEquals(String.class, bean.getSomeClass());
        assertEquals(new Date(0), bean.getDate());
        assertEquals(new BigInteger("1234567890987654321"), bean.getBigInteger());
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
        String json = I.json(group);
        assertEquals("{\"members\":[{\"age\":\"1\"},{\"age\":\"1\"},{\"age\":\"2\"}]}", json);
    }

    @Test
    public void testEscapeDoubleQuote() {
        Person person = I.make(Person.class);
        person.setFirstName("\"");

        // write
        String json = I.json(person);
        assertEquals("{\"age\":\"0\",\"firstName\":\"\\\"\"}", json);

        // read
        person = I.json(Person.class, json);
        assertEquals("\"", person.getFirstName());
    }

    @Test
    public void testEscapeBackSlash() {
        Person person = I.make(Person.class);
        person.setFirstName("\\");

        // write
        String json = I.json(person);
        assertEquals("{\"age\":\"0\",\"firstName\":\"\\\\\"}", json);

        // read
        person = I.json(Person.class, json);
        assertEquals("\\", person.getFirstName());
    }

    @Test
    public void nullJSON() {
        assertNotNull(I.json(Person.class, null));
    }

    @Test
    public void emptyJSON() {
        assertNotNull(I.json(Person.class, ""));
    }

    @Test
    public void invlidJSON1() {
        assertNotNull(I.json(Person.class, "15"));
    }

    @Test
    public void invlidJSON2() {
        assertNotNull(I.json(Person.class, "@"));
    }

    @Test(expected = ClassCircularityError.class)
    public void testCyclicRootNode() {
        ChainBean chain = I.make(ChainBean.class);
        chain.setNext(chain);

        assertEquals("{\"next\":{}}", I.json(chain));
    }
}
