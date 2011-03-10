/*
 * Copyright (C) 2011 Nameless Production Committee.
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
package ezbean.serialization;

import static ezunit.Ezunit.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import ezbean.EzbeanTest;
import ezbean.I;
import ezbean.sample.bean.BuiltinBean;
import ezbean.sample.bean.CompatibleKeyMap;
import ezbean.sample.bean.GenericPersonBean;
import ezbean.sample.bean.IncompatibleKeyMap;
import ezbean.sample.bean.NestedCollection;
import ezbean.sample.bean.NestingList;
import ezbean.sample.bean.Person;
import ezbean.sample.bean.Primitive;
import ezbean.sample.bean.School;
import ezbean.sample.bean.SchoolEnum;
import ezbean.sample.bean.StringList;
import ezbean.sample.bean.StringMap;
import ezbean.sample.bean.Student;
import ezbean.sample.bean.TransientBean;
import ezunit.CleanRoom;

/**
 * @version 2011/03/07 12:35:17
 */
public class XMLSerializationTest {

    /** The temporaries. */
    @Rule
    public static final CleanRoom room = new CleanRoom();

    /** The serialization file. */
    private static final Path testFile = room.locateFile("config.xml");

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
        I.xml(nestingList, testFile);

        // read
        nestingList = I.xml(testFile, I.make(NestingList.class));

        assertNotNull(nestingList);

        root = nestingList.getNesting();
        assertNotNull(root);
        assertEquals(2, root.size());

        list1 = root.get(0);
        assertNotNull(list1);
        assertEquals(1, list1.get(0).intValue());
        assertEquals(2, list1.get(1).intValue());
        assertEquals(3, list1.get(2).intValue());

        list2 = root.get(1);
        assertNotNull(list2);
        assertEquals(10, list2.get(0).intValue());
        assertEquals(11, list2.get(1).intValue());
        assertEquals(12, list2.get(2).intValue());
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
        I.xml(primitive, testFile);

        // read
        primitive = I.xml(testFile, I.make(Primitive.class));
        assertNotNull(primitive);
        assertEquals(true, primitive.isBoolean());
        assertEquals('c', primitive.getChar());
        assertEquals((byte) 10, primitive.getByte());
        assertEquals(3.14, primitive.getDouble(), 0);
        assertEquals(1.41421356F, primitive.getFloat(), 0);
        assertEquals(-256, primitive.getInt());
        assertEquals(-987654321L, primitive.getLong());
        assertEquals((short) 21, primitive.getShort());
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

        StringList stringList = I.make(StringList.class);
        stringList.setList(list);

        // write
        I.xml(stringList, testFile);

        // read
        stringList = I.xml(testFile, I.make(StringList.class));
        assertNotNull(stringList);

        list = stringList.getList();
        assertEquals("<", list.get(0));
        assertEquals(">", list.get(1));
        assertEquals("\"", list.get(2));
        assertEquals("'", list.get(3));
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

        StringList stringList = I.make(StringList.class);
        stringList.setList(list);

        // write
        I.xml(stringList, testFile);

        // read
        stringList = I.xml(testFile, I.make(StringList.class));
        assertNotNull(stringList);

        list = stringList.getList();
        assertEquals("ﾃｽﾄ", list.get(0));
        assertEquals("です", list.get(1));
        assertEquals("(´･ω･`)", list.get(2));
    }

    /**
     * Test Map with invalid element name.
     */
    @Test
    public void testReadAndWrite8() throws Exception {
        StringMap stringMap = I.make(StringMap.class);
        Map<String, String> map = new HashMap<String, String>();
        map.put("1", "one");
        map.put("2", "two");
        map.put("3", "three");

        stringMap.setMap(map);

        // write
        I.xml(stringMap, testFile);

        // read
        stringMap = I.xml(testFile, I.make(StringMap.class));
        assertNotNull(stringMap);

        map = stringMap.getMap();
        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals("one", map.get("1"));
        assertEquals("two", map.get("2"));
        assertEquals("three", map.get("3"));
    }

    /**
     * Test Map with invalid element name.
     */
    @Test
    public void testReadAndWrite9() throws Exception {
        StringMap stringMap = I.make(StringMap.class);
        Map<String, String> map = new HashMap<String, String>();
        map.put("\"", "one");
        map.put(" ", "two");
        map.put("<", "three");

        stringMap.setMap(map);

        // write
        I.xml(stringMap, testFile);

        // read
        stringMap = I.xml(testFile, I.make(StringMap.class));
        assertNotNull(stringMap);

        map = stringMap.getMap();
        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals("one", map.get("\""));
        assertEquals("two", map.get(" "));
        assertEquals("three", map.get("<"));
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
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(CompatibleKeyMap.class));
        assertNotNull(bean);

        map = bean.getIntegerKey();
        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals(String.class, map.get(1));
        assertEquals(Class.class, map.get(2));
        assertEquals(Integer.class, map.get(3));
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
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(IncompatibleKeyMap.class));
        assertNotNull(bean);

        map = bean.getIncompatible();
        assertNotNull(map);
        assertEquals(0, map.size());
    }

    /**
     * Test {@link Enum}.
     */
    @Test
    public void javaEnum() throws Exception {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setSchoolEnum(SchoolEnum.Miator);

        // write
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(BuiltinBean.class));
        assertNotNull(bean);
        assertEquals(SchoolEnum.Miator, bean.getSchoolEnum());
    }

    /**
     * Test {@link Date}.
     */
    @Test
    public void javaDate() throws Exception {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setDate(new Date(0L));

        // write
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(BuiltinBean.class));
        assertNotNull(bean);
        assertEquals(new Date(0L), bean.getDate());

        // validate format
        assertXPathEqual("1970-01-01T09:00:00", testFile, "/BuiltinBean/@date");
    }

    /**
     * Test {@link Class}.
     */
    @Test
    public void javaClass() throws Exception {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setSomeClass(EzbeanTest.class);

        // write
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(BuiltinBean.class));
        assertNotNull(bean);
        assertEquals(EzbeanTest.class, bean.getSomeClass());

        // validate format
        assertXPathEqual(EzbeanTest.class.getName(), testFile, "/BuiltinBean/@someClass");
    }

    /**
     * Test {@link File}.
     */
    @Test
    public void javaFile() throws Exception {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setFile(testFile.toFile());

        // write
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(BuiltinBean.class));
        assertNotNull(bean);
        assertEquals(testFile.toFile(), bean.getFile());
    }

    /**
     * Test {@link Path}.
     */
    @Test
    public void javaPath() throws Exception {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setPath(testFile);

        // write
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(BuiltinBean.class));
        assertNotNull(bean);
        assertEquals(testFile, bean.getPath());
    }

    /**
     * Test {@link ezbean.io.FilePath}.
     */
    @Test
    public void ezbeanFile() throws Exception {
        BuiltinBean bean = I.make(BuiltinBean.class);
        bean.setFile(testFile.toFile());

        // write
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(BuiltinBean.class));
        assertNotNull(bean);
        assertEquals(testFile.toFile(), bean.getFile());
    }

    @Test
    public void transientProperty() throws Exception {
        TransientBean bean = I.make(TransientBean.class);
        bean.setNone(10);
        bean.setBoth(20);

        // write
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(TransientBean.class));
        assertNotNull(bean);
        assertEquals(10, bean.getNone());
        assertEquals(0, bean.getBoth());
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
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(NestedCollection.class));
        assertNotNull(bean);
        map = bean.getNest();
        assertNotNull(bean);
        list = map.get("a");
        assertNotNull(list);
        person = list.get(0);
        assertNotNull(person);
        assertEquals(17, person.getAge());
    }

    /**
     * Test List.
     */
    @Test
    public void readAndWriteList() throws Exception {
        StringList stringList = I.make(StringList.class);
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("2");
        list.add("3");

        stringList.setList(list);

        // write
        I.xml(stringList, testFile);

        // read
        stringList = I.xml(testFile, I.make(StringList.class));
        assertNotNull(stringList);

        list = stringList.getList();
        assertNotNull(list);
        assertEquals(3, list.size());
        assertEquals("1", list.get(0));
        assertEquals("2", list.get(1));
        assertEquals("3", list.get(2));

        // list must not have ez:key attribute
        assertXPathEqual("String", testFile, "local-name(//String[1])");
        assertXPathEqual("", testFile, "//String[1]/@ez:key");
    }

    @Test
    public void readAndWriteNullContainsList() throws Exception {
        List<String> list = new ArrayList();
        list.add(null);
        list.add("");
        list.add(null);
        list.add("k-on");
        list.add(null);

        StringList bean = I.make(StringList.class);
        bean.setList(list);

        // write
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(StringList.class));
        assertNotNull(bean);

        list = bean.getList();
        assertNotNull(list);
        assertEquals(5, list.size());
        assertNull(list.get(0));
        assertEquals("", list.get(1));
        assertNull(list.get(2));
        assertEquals("k-on", list.get(3));
        assertNull(list.get(4));
    }

    /**
     * Test Map.
     */
    @Test
    public void readAndWriteMap() throws Exception {
        StringMap stringMap = I.make(StringMap.class);
        Map<String, String> map = new HashMap<String, String>();
        map.put("one", "one");
        map.put("two", "two");
        map.put("three", "three");

        stringMap.setMap(map);

        // write
        I.xml(stringMap, testFile);

        // read
        stringMap = I.xml(testFile, I.make(StringMap.class));
        assertNotNull(stringMap);

        map = stringMap.getMap();
        assertNotNull(map);
        assertEquals(3, map.size());
        assertEquals("one", map.get("one"));
        assertEquals("two", map.get("two"));
        assertEquals("three", map.get("three"));

        // map must have ez:key attribute
        assertXPathEqual("two", testFile, "//String[1]/@ez:key");
    }

    @Test
    public void readAndWriteNullContainsMap() throws Exception {
        Map<String, String> map = new HashMap();
        map.put("", "");
        map.put("null", null);

        StringMap bean = I.make(StringMap.class);
        bean.setMap(map);

        // write
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(StringMap.class));
        assertNotNull(bean);

        map = bean.getMap();
        assertNotNull(map);
        assertEquals(2, map.size());
        assertEquals("", map.get(""));
        assertNull(map.get("null"));
        assertTrue(map.containsKey("null"));
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
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(GenericPersonBean.class));
        assertNotNull(bean);

        Person person = bean.getGeneric();
        assertNotNull(person);
        assertEquals("saki", person.getLastName());
        list = bean.getGenericList();
        assertNotNull(list);

        person = list.get(0);
        assertNotNull(person);
        assertEquals("saki", person.getLastName());
        person = list.get(1);
        assertNotNull(person);
        assertEquals("nodoka", person.getLastName());
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
        I.xml(bean, testFile);

        // read
        bean = I.xml(testFile, I.make(GenericPersonBean.class));
        assertNotNull(bean);

        list = bean.getGenericList();
        assertNotNull(list);
        assertEquals("Koume", list.get(0).getFirstName());
        assertEquals("Koume", list.get(1).getFirstName());
        assertEquals("Akiko", list.get(2).getFirstName());
        assertEquals("Tamaki", list.get(3).getFirstName());
        assertEquals("Akiko", list.get(4).getFirstName());
        assertEquals("Tamaki", list.get(5).getFirstName());
        assertEquals(list.get(0), list.get(1));
        assertEquals(list.get(2), list.get(4));
        assertEquals(list.get(3), list.get(5));
    }

    @Test
    public void multipleAttributeReference() throws Exception {
        StringList stringList = I.make(StringList.class);
        List<String> list = new ArrayList<String>();
        list.add("1");
        list.add("1");
        list.add("2");
        list.add("3");
        list.add("2");
        list.add("3");

        stringList.setList(list);

        // write
        I.xml(stringList, testFile);

        // read
        stringList = I.xml(testFile, I.make(StringList.class));
        assertNotNull(stringList);

        list = stringList.getList();
        assertNotNull(list);
        assertEquals("1", list.get(0));
        assertEquals("1", list.get(1));
        assertEquals("2", list.get(2));
        assertEquals("3", list.get(3));
        assertEquals("2", list.get(4));
        assertEquals("3", list.get(5));
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
        I.xml(checker, testFile);

        // read
        checker = I.xml(testFile, I.make(Checker.class));
        assertNotNull(checker);
        assertEquals(3, checker.size);
    }

    /**
     * @version 2010/01/08 17:11:38
     */
    protected static class Checker extends StringList {

        private int size = -1;

        /**
         * @see ezbean.sample.bean.StringList#setList(java.util.List)
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
        I.xml(student, testFile);

        // read
        student = I.xml(testFile, I.make(Student.class));
        assertNotNull(student);
        assertEquals(17, student.getAge());
        assertEquals("Himeko", student.getFirstName());
        assertEquals("Kurusugawa", student.getLastName());

        school = student.getSchool();
        assertEquals("OtoTatibana", school.getName());
        assertEquals(2, school.getStudents().size());

        student2 = school.getStudents().get(0);
        assertNotNull(student2);
        assertEquals(17, student2.getAge());
        assertEquals("Himeko", student2.getFirstName());
        assertEquals("Kurusugawa", student2.getLastName());
        assertEquals(student, student2);
    }

    /**
     * Test method for {@link ezbean.I#read(java.io.File, java.lang.Object)}.
     */
    @Test
    public void testReadWithNull1() throws Exception {
        I.xml((Path) null, (Object) null);
    }

    /**
     * Test method for {@link ezbean.I#read(java.io.File, java.lang.Object)}.
     */
    @Test
    public void testReadWithNull2() throws Exception {
        assertNotNull(testFile);
        I.xml(testFile, (Object) null);
    }

    /**
     * Test method for {@link ezbean.I#read(java.io.File, java.lang.Object)}.
     */
    @Test
    public void testReadWithNull3() throws Exception {
        I.xml((Path) null, Student.class);
    }

    /**
     * Test method for {@link ezbean.I#write(java.lang.Object, java.io.File)}.
     */
    @Test
    public void testWriteWithNull1() throws Exception {
        I.xml((Object) null, (Path) null);
    }

    /**
     * Test method for {@link ezbean.I#write(java.lang.Object, java.io.File)}.
     */
    @Test
    public void testWriteWithNull2() throws Exception {
        assertNotNull(testFile);
        I.xml((Object) null, testFile);
    }

    /**
     * Test writing Ezbean against for not existing file.
     */
    @Test
    public void testWriteNotExistingFile1() throws Exception {
        Path notExist = room.locateAbsent("file");

        assertTrue(Files.notExists(notExist));

        Person person = I.make(Person.class);
        person.setAge(1);

        // write Ezbean
        I.xml(person, notExist);

        // test
        assertTrue(Files.exists(notExist));
    }

    /**
     * Test writing Ezbean against for not existing file which has a deep tree.
     */
    @Test
    public void testWriteNotExistingFile2() throws Exception {
        Path root = room.locateAbsent("directory");
        Path notExist = root.resolve("not-exist/not-exist/not-exist");
        assertTrue(Files.notExists(notExist));

        Person person = I.make(Person.class);
        person.setAge(1);

        // write Ezbean
        I.xml(person, notExist);

        // test
        assertTrue(Files.exists(notExist));
    }
}
