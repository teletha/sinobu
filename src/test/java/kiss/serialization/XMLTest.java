/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.serialization;

import static antibug.AntiBug.*;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kiss.I;
import kiss.SinobuTest;
import kiss.sample.bean.BuiltinBean;
import kiss.sample.bean.CompatibleKeyMap;
import kiss.sample.bean.FieldProperty;
import kiss.sample.bean.GenericFieldProperty;
import kiss.sample.bean.GenericPersonBean;
import kiss.sample.bean.IncompatibleKeyMap;
import kiss.sample.bean.NestedCollection;
import kiss.sample.bean.NestingList;
import kiss.sample.bean.Person;
import kiss.sample.bean.Primitive;
import kiss.sample.bean.School;
import kiss.sample.bean.SchoolEnum;
import kiss.sample.bean.StringList;
import kiss.sample.bean.StringListProperty;
import kiss.sample.bean.StringMap;
import kiss.sample.bean.StringMapProperty;
import kiss.sample.bean.Student;
import kiss.sample.bean.TransientBean;

import org.junit.Rule;
import org.junit.Test;

import antibug.CleanRoom;
import antibug.xml.XML;

/**
 * @version 2012/11/09 10:45:34
 */
public class XMLTest {

    /** The temporaries. */
    @Rule
    public static final CleanRoom room = new CleanRoom();

    /** The serialization file. */
    private static final Path config = room.locateFile("config.xml");

    /** The namespace definition for xpath. */
    private static Map<String, String> namespaces = new HashMap();

    static {
        namespaces.put("ss", "sinobu");
    }

    @Test
    public void linefeed() throws Exception {
        StringList list = I.make(StringList.class);
        list.add("A\r\nA");

        // write
        I.write(list, config, false);

        // read
        list = I.read(config, I.make(StringList.class));
        assert list.get(0).equals("A\r\nA");
    }

    @Test
    public void list() throws Exception {
        Student student = I.make(Student.class);
        student.setName("test");

        School school = I.make(School.class);
        school.addStudent(student);

        // write
        I.write(school, config, false);

        // read
        school = I.read(config, I.make(School.class));
        assert school.getStudents().get(0).getName().equals("test");
    }

    @Test
    public void listModel() throws Exception {
        StringList list = I.make(StringList.class);
        list.add("10");
        list.add("20");

        // write
        I.write(list, config, false);

        // read
        list = I.read(config, I.make(StringList.class));
        assert list.size() == 2;
        assert list.get(0).equals("10");
        assert list.get(1).equals("20");
    }

    @Test
    public void map() throws Exception {
        Person teacher = I.make(Person.class);
        teacher.setFirstName("test");

        Map<String, Person> teachers = new HashMap();
        teachers.put("role", teacher);

        School school = I.make(School.class);
        school.setTeachers(teachers);

        // write
        I.write(school, config, false);

        // read
        school = I.read(config, I.make(School.class));
        assert school.getTeachers().get("role").getFirstName().equals("test");
    }

    @Test
    public void mapModel() throws Exception {
        StringMap map = I.make(StringMap.class);
        map.put("one", "1");
        map.put("two", "2");

        // write
        I.write(map, config, false);

        // read
        map = I.read(config, I.make(StringMap.class));
        assert map.size() == 2;
        assert map.get("one").equals("1");
        assert map.get("two").equals("2");
    }

    /**
     * Test nesting List.
     */
    @Test
    public void testReadAndWrite3() throws Exception {
        NestingList nestingList = I.make(NestingList.class);
        List<List<Integer>> root = new ArrayList<List<Integer>>();

        List<Integer> list1 = new ArrayList();
        list1.add(1);
        list1.add(2);
        list1.add(3);
        root.add(list1);

        List<Integer> list2 = new ArrayList();
        list2.add(10);
        list2.add(11);
        list2.add(12);
        root.add(list2);

        nestingList.setNesting(root);

        // write
        I.write(nestingList, config, false);

        // read
        nestingList = I.read(config, I.make(NestingList.class));

        assert nestingList != null;

        root = nestingList.getNesting();
        assert root != null;
        assert root.size() == 2;

        list1 = root.get(0);
        assert list1 != null;
        assert list1.get(0).intValue() == 1;
        assert list1.get(1).intValue() == 2;
        assert list1.get(2).intValue() == 3;

        list2 = root.get(1);
        assert list2 != null;
        assert list2.get(0).intValue() == 10;
        assert list2.get(1).intValue() == 11;
        assert list2.get(2).intValue() == 12;
    }

    /**
     * Test Primitives.
     */
    @Test
    public void testReadAndWrite4() throws Exception {
        Primitive primitive = I.make(Primitive.class);
        primitive.setBoolean(true);
        primitive.setChar('c');
        primitive.setByte((byte) 10);
        primitive.setDouble(3.14);
        primitive.setFloat(1.41421356F);
        primitive.setInt(-256);
        primitive.setLong(-987654321);
        primitive.setShort((short) 21);

        // write
        I.write(primitive, config, false);

        // read
        primitive = I.read(config, I.make(Primitive.class));
        assert primitive != null;
        assert primitive.isBoolean();
        assert primitive.getChar() == 'c';
        assert primitive.getByte() == 10;
        assert primitive.getDouble() == 3.14d;
        assert primitive.getFloat() == 1.41421356f;
        assert primitive.getInt() == -256;
        assert primitive.getLong() == -987654321L;
        assert primitive.getShort() == 21;
    }

    /**
     * Test invalid XML charcater.
     */
    @Test
    public void testReadAndWrite5() throws Exception {
        List<String> list = new ArrayList();
        list.add("<");
        list.add(">");
        list.add("\"");
        list.add("'");

        StringListProperty stringList = I.make(StringListProperty.class);
        stringList.setList(list);

        // write
        I.write(stringList, config, false);

        // read
        stringList = I.read(config, I.make(StringListProperty.class));
        assert stringList != null;

        list = stringList.getList();
        assert list.get(0).equals("<");
        assert list.get(1).equals(">");
        assert list.get(2).equals("\"");
        assert list.get(3).equals("'");
    }

    /**
     * Test non-ASCII character.
     */
    @Test
    public void testReadAndWrite6() throws Exception {
        List<String> list = new ArrayList();
        list.add("ﾃｽﾄ");
        list.add("です");
        list.add("(´･ω･`)");

        StringListProperty stringList = I.make(StringListProperty.class);
        stringList.setList(list);

        // write
        I.write(stringList, config, false);

        // read
        stringList = I.read(config, I.make(StringListProperty.class));
        assert stringList != null;

        list = stringList.getList();
        assert list.get(0).equals("ﾃｽﾄ");
        assert list.get(1).equals("です");
        assert list.get(2).equals("(´･ω･`)");
    }

    /**
     * Test Map with invalid element name.
     */
    @Test
    public void testReadAndWrite8() throws Exception {
        StringMapProperty stringMap = I.make(StringMapProperty.class);
        Map<String, String> map = new HashMap<String, String>();
        map.put("1", "one");
        map.put("2", "two");
        map.put("3", "three");

        stringMap.setMap(map);

        // write
        I.write(stringMap, config, false);

        // read
        stringMap = I.read(config, I.make(StringMapProperty.class));
        assert stringMap != null;

        map = stringMap.getMap();
        assert map != null;
        assert map.size() == 3;
        assert map.get("1").equals("one");
        assert map.get("2").equals("two");
        assert map.get("3").equals("three");
    }

    /**
     * Test Map with invalid element name.
     */
    @Test
    public void testReadAndWrite9() throws Exception {
        StringMapProperty stringMap = I.make(StringMapProperty.class);
        Map<String, String> map = new HashMap<String, String>();
        map.put("\"", "one");
        map.put(" ", "two");
        map.put("<", "three");

        stringMap.setMap(map);

        // write
        I.write(stringMap, config, false);

        // read
        stringMap = I.read(config, I.make(StringMapProperty.class));
        assert stringMap != null;

        map = stringMap.getMap();
        assert map != null;
        assert map.size() == 3;
        assert map.get("\"").equals("one");
        assert map.get(" ").equals("two");
        assert map.get("<").equals("three");
    }

    /**
     * Test Map with non-string key.
     */
    @Test
    public void compatibleKeyMap() throws Exception {
        CompatibleKeyMap bean = I.make(CompatibleKeyMap.class);
        Map<Integer, Class> map = new HashMap<Integer, Class>();
        map.put(1, String.class);
        map.put(2, Class.class);
        map.put(3, Integer.class);

        bean.setIntegerKey(map);

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(CompatibleKeyMap.class));
        assert bean != null;

        map = bean.getIntegerKey();
        assert map != null;
        assert map.size() == 3;
        assert map.get(1).equals(String.class);
        assert map.get(2).equals(Class.class);
        assert map.get(3).equals(Integer.class);
    }

    /**
     * Test Map with non-string key.
     */
    @Test
    public void incompatibleKeyMap() throws Exception {
        IncompatibleKeyMap bean = I.make(IncompatibleKeyMap.class);
        Map<Serializable, Class> map = new HashMap<Serializable, Class>();
        map.put("1", String.class);
        map.put("2", Class.class);
        map.put("3", Integer.class);

        bean.setIncompatible(map);

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(IncompatibleKeyMap.class));
        assert bean != null;

        map = bean.getIncompatible();
        assert map != null;
        assert map.size() == 0;
    }

    /**
     * Test {@link Enum}.
     */
    @Test
    public void javaEnum() throws Exception {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setSchoolEnum(SchoolEnum.Miator);

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(BuiltinBean.class));
        assert bean != null;
        assert bean.getSchoolEnum().equals(SchoolEnum.Miator);
    }

    /**
     * Test {@link Date}.
     */
    @Test
    public void javaDate() throws Exception {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setDate(new Date(0L));

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(BuiltinBean.class));
        assert bean != null;
        assert bean.getDate().equals(new Date(0L));

        // validate format
        XML xml = xml(config);
        assert xml.select("/BuiltinBean/@date").getTextContent().equals("1970-01-01T09:00:00");
    }

    /**
     * Test {@link Class}.
     */
    @Test
    public void javaClass() throws Exception {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setSomeClass(SinobuTest.class);

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(BuiltinBean.class));
        assert bean != null;
        assert bean.getSomeClass().equals(SinobuTest.class);

        // validate format
        XML xml = xml(config);
        assert xml.has("/BuiltinBean/@someClass");
    }

    /**
     * Test {@link File}.
     */
    @Test
    public void javaFile() throws Exception {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setFile(config.toFile());

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(BuiltinBean.class));
        assert bean != null;
        assert bean.getFile().equals(config.toFile());
    }

    /**
     * Test {@link Path}.
     */
    @Test
    public void javaPath() throws Exception {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setPath(config);

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(BuiltinBean.class));
        assert bean != null;
        assert bean.getPath().equals(config);
    }

    @Test
    public void transientProperty() throws Exception {
        TransientBean bean = I.make(TransientBean.class);
        bean.setNone(10);
        bean.setBoth(20);

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(TransientBean.class));
        assert bean != null;
        assert bean.getNone() == 10;
        assert bean.getBoth() == 0;
    }

    @Test
    public void publicFieldProperty() throws Exception {
        FieldProperty bean = I.make(FieldProperty.class);
        bean.publicField = "me";

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(FieldProperty.class));
        assert bean != null;
        assert bean.publicField.equals("me");
    }

    @Test
    public void genericFieldProperty() throws Exception {
        List<String> list = new ArrayList();
        list.add("test");

        GenericFieldProperty bean = I.make(GenericFieldProperty.class);
        bean.genericField = list;

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(GenericFieldProperty.class));
        assert bean != null;
        assert bean.genericField.get(0).equals("test");
    }

    @Test
    public void readAndWriteNestedCollection() throws Exception {
        NestedCollection bean = I.make(NestedCollection.class);
        Map<String, List<Person>> map = new HashMap();
        List<Person> list = new ArrayList();
        Person person = I.make(Person.class);
        person.setAge(17);
        list.add(person);
        map.put("a", list);
        bean.setNest(map);

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(NestedCollection.class));
        assert bean != null;
        map = bean.getNest();
        assert bean != null;
        list = map.get("a");
        assert list != null;
        person = list.get(0);
        assert person != null;
        assert person.getAge() == 17;
    }

    /**
     * Test List.
     */
    @Test
    public void readAndWriteList() throws Exception {
        StringListProperty stringList = I.make(StringListProperty.class);
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add("3");

        stringList.setList(list);

        // write
        I.write(stringList, config, false);

        // read
        stringList = I.read(config, I.make(StringListProperty.class));
        assert stringList != null;

        list = stringList.getList();
        assert list != null;
        assert list.size() == 3;
        assert list.get(0).equals("1");
        assert list.get(1).equals("2");
        assert list.get(2).equals("3");

        // list must not have ez:key attribute
        XML xml = xml(config);
        assert xml.has("//String[1]");
        assert !xml.has("//String[1]/@ss:key", "ss", "sinobu");
    }

    @Test
    public void readAndWriteNullContainsList() throws Exception {
        List<String> list = new ArrayList();
        list.add(null);
        list.add("");
        list.add(null);
        list.add("k-on");
        list.add(null);

        StringListProperty bean = I.make(StringListProperty.class);
        bean.setList(list);

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(StringListProperty.class));
        assert bean != null;

        list = bean.getList();
        assert list != null;
        assert list.size() == 5;
        assert list.get(0) == null;
        assert list.get(1).equals("");
        assert list.get(2) == null;
        assert list.get(3).equals("k-on");
        assert list.get(4) == null;
    }

    /**
     * Test Map.
     */
    @Test
    public void readAndWriteMap() throws Exception {
        StringMapProperty stringMap = I.make(StringMapProperty.class);
        Map<String, String> map = new HashMap<String, String>();
        map.put("one", "one");
        map.put("two", "two");
        map.put("three", "three");

        stringMap.setMap(map);

        // write
        I.write(stringMap, config, false);

        // read
        stringMap = I.read(config, I.make(StringMapProperty.class));
        assert stringMap != null;

        map = stringMap.getMap();
        assert map != null;
        assert map.size() == 3;
        assert map.get("one").equals("one");
        assert map.get("two").equals("two");
        assert map.get("three").equals("three");

        // map must have ez:key attribute
        XML xml = xml(config);
        assert xml.select("//String[1]/@ss:key", "ss", "sinobu").getTextContent().equals("two");
    }

    @Test
    public void readAndWriteNullContainsMap() throws Exception {
        Map<String, String> map = new HashMap();
        map.put("", "");
        map.put("null", null);

        StringMapProperty bean = I.make(StringMapProperty.class);
        bean.setMap(map);

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(StringMapProperty.class));
        assert bean != null;

        map = bean.getMap();
        assert map != null;
        assert map.size() == 2;
        assert map.get("").equals("");
        assert map.get("null") == null;
        assert map.containsKey("null");
    }

    @Test
    public void readAndWriteGeneric() throws Exception {
        Person saki = I.make(Person.class);
        saki.setFirstName("miyanaga");
        saki.setLastName("saki");

        Person nodotti = I.make(Person.class);
        nodotti.setFirstName("hanamura");
        nodotti.setLastName("nodoka");

        GenericPersonBean bean = I.make(GenericPersonBean.class);
        bean.setGeneric(saki);

        List<Person> list = new ArrayList();
        list.add(saki);
        list.add(nodotti);
        bean.setGenericList(list);

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(GenericPersonBean.class));
        assert bean != null;

        Person person = bean.getGeneric();
        assert person != null;
        assert person.getLastName().equals("saki");
        list = bean.getGenericList();
        assert list != null;

        person = list.get(0);
        assert person != null;
        assert person.getLastName().equals("saki");
        person = list.get(1);
        assert person != null;
        assert person.getLastName().equals("nodoka");
    }

    @Test
    public void multipleReference() throws Exception {
        Person koume = I.make(Person.class);
        koume.setFirstName("Koume");
        koume.setLastName("Suzukawa");

        Person akiko = I.make(Person.class);
        akiko.setFirstName("Akiko");
        akiko.setLastName("Ogasawara");

        Person tamaki = I.make(Person.class);
        tamaki.setFirstName("Tamaki");
        tamaki.setLastName("Isigaki");

        GenericPersonBean bean = I.make(GenericPersonBean.class);
        List<Person> list = new ArrayList();
        list.add(koume);
        list.add(koume);
        list.add(akiko);
        list.add(tamaki);
        list.add(akiko);
        list.add(tamaki);

        bean.setGenericList(list);

        // write
        I.write(bean, config, false);

        // read
        bean = I.read(config, I.make(GenericPersonBean.class));
        assert bean != null;

        list = bean.getGenericList();
        assert list != null;
        assert list.get(0).getFirstName().equals("Koume");
        assert list.get(1).getFirstName().equals("Koume");
        assert list.get(2).getFirstName().equals("Akiko");
        assert list.get(3).getFirstName().equals("Tamaki");
        assert list.get(4).getFirstName().equals("Akiko");
        assert list.get(5).getFirstName().equals("Tamaki");
        assert list.get(1).equals(list.get(0));
        assert list.get(4).equals(list.get(2));
        assert list.get(5).equals(list.get(3));
    }

    @Test
    public void multipleAttributeReference() throws Exception {
        StringListProperty stringList = I.make(StringListProperty.class);
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("2");
        list.add("3");

        stringList.setList(list);

        // write
        I.write(stringList, config, false);

        // read
        stringList = I.read(config, I.make(StringListProperty.class));
        assert stringList != null;

        list = stringList.getList();
        assert list != null;
        assert list.get(0).equals("1");
        assert list.get(1).equals("1");
        assert list.get(2).equals("2");
        assert list.get(3).equals("3");
        assert list.get(4).equals("2");
        assert list.get(5).equals("3");
    }

    @Test
    public void setCompleteObject() throws Exception {
        Checker checker = I.make(Checker.class);
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add("3");

        checker.setList(list);

        // write
        I.write(checker, config, false);

        // read
        checker = I.read(config, I.make(Checker.class));
        assert checker != null;
        assert checker.size == 3;
    }

    /**
     * @version 2010/01/08 17:11:38
     */
    protected static class Checker extends StringListProperty {

        private int size = -1;

        /**
         * @see kiss.sample.bean.StringListProperty#setList(java.util.List)
         */
        @Override
        public void setList(List<String> list) {
            super.setList(list);

            size = list.size();
        }
    }

    /**
     * Test circular reference.
     */
    @Test
    public void circularReference() throws Exception {
        School school = I.make(School.class);
        school.setName("OtoTatibana");

        Student student = I.make(Student.class);
        student.setAge(17);
        student.setFirstName("Himeko");
        student.setLastName("Kurusugawa");
        student.setSchool(school);

        Student student2 = I.make(Student.class);
        student2.setAge(17);
        student2.setFirstName("Tikane");
        student2.setLastName("Himemiya");
        student2.setSchool(school);

        List<Student> students = new ArrayList();
        students.add(student);
        students.add(student2);

        school.setStudents(students);

        // write
        I.write(student, config, false);

        // read
        student = I.read(config, I.make(Student.class));
        assert student != null;
        assert student.getAge() == 17;
        assert student.getFirstName().equals("Himeko");
        assert student.getLastName().equals("Kurusugawa");

        school = student.getSchool();
        assert school.getName().equals("OtoTatibana");
        assert school.getStudents().size() == 2;

        student2 = school.getStudents().get(0);
        assert student2 != null;
        assert student2.getAge() == 17;
        assert student2.getFirstName().equals("Himeko");
        assert student2.getLastName().equals("Kurusugawa");
        assert student2.equals(student);
    }
}
