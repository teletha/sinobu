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
package ezbean.model;

import static org.junit.Assert.*;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import org.junit.Test;

import ezbean.I;
import ezbean.model.Model;
import ezbean.model.ModelWalkListener;
import ezbean.model.ModelWalker;
import ezbean.model.Property;
import ezbean.sample.bean.Person;
import ezbean.sample.bean.School;
import ezbean.sample.bean.StringMap;
import ezbean.sample.bean.Student;


/**
 * DOCUMENT.
 * 
 * @version 2008/06/17 14:25:15
 */
public class ModelWalkerTest {

    /**
     * Test simple traverse.
     */
    @Test
    public void testTraverse1() throws Exception {
        // root
        Person person = I.make(Person.class);

        // result collector
        Collector collector = new Collector();

        // traverse
        ModelWalker walker = new ModelWalker(person);
        walker.addListener(collector);
        walker.traverse();

        // assert
        assertEquals(2, collector.enterNodes.size());
        assertEquals(2, collector.leaveNodes.size());
    }

    /**
     * Test simple traverse.
     */
    @Test
    public void testTraverse2() throws Exception {
        // root
        Person person = I.make(Person.class);
        person.setFirstName("Mizuho");
        person.setLastName("Miyanokoji");
        person.setAge(18);

        // result collector
        Collector collector = new Collector();

        // traverse
        ModelWalker walker = new ModelWalker(person);
        walker.addListener(collector);
        walker.traverse();

        // assert
        assertEquals(4, collector.enterNodes.size());
        assertEquals(4, collector.leaveNodes.size());

        person = (Person) collector.enterNodes.get(0).value;
        assertEquals(18, person.getAge());
        assertEquals("Mizuho", person.getFirstName());
        assertEquals("Miyanokoji", person.getLastName());
    }

    /**
     * Test traverse with property path.
     */
    @Test
    public void testTraverseWithRoute01() throws Exception {
        // root
        Person person = I.make(Person.class);
        person.setFirstName("Sion");
        person.setLastName("Jujo");
        person.setAge(19);

        // result collector
        Collector collector = new Collector();

        // traverse
        ModelWalker walker = new ModelWalker(person);
        walker.addListener(collector);
        walker.traverse(Arrays.asList("age"));

        // assert
        assertEquals(2, collector.enterNodes.size());
        assertEquals(2, collector.leaveNodes.size());
    }

    /**
     * Test without listeners.
     */
    @Test
    public void testTraverseWitthoutListener1() throws Exception {
        Person person = I.make(Person.class);
        person.setFirstName("Itiko");
        person.setLastName("Takasima");
        person.setAge(-1);

        ModelWalker walker = new ModelWalker(person);
        assertEquals("Itiko", walker.traverse(Arrays.asList("firstName")));
        assertEquals("Takasima", walker.traverse(Arrays.asList("lastName")));
        assertEquals(-1, walker.traverse(Arrays.asList("age")));
    }

    /**
     * Test unexpected property name.
     */
    @Test
    public void testTraverseWithUnexpectedPropertyName() throws Exception {
        Person person = I.make(Person.class);

        ModelWalker walker = new ModelWalker(person);
        assertEquals(null, walker.traverse(Arrays.asList("unexpected")));
    }

    /**
     * Test without listeners.
     */
    @Test
    public void testTraverseWitthoutListener2() throws Exception {
        Map map = new HashMap();
        map.put("item1", "1");
        map.put("item2", "2");
        map.put("item3", "3");

        StringMap stringMap = new StringMap();
        stringMap.setMap(map);

        ModelWalker walker = new ModelWalker(stringMap);
        assertEquals("1", walker.traverse(Arrays.asList("map", "item1")));
        assertEquals("2", walker.traverse(Arrays.asList("map", "item2")));
        assertEquals("3", walker.traverse(Arrays.asList("map", "item3")));
    }

    /**
     * Reuse {@link ModelWalker} with cyclick nodes.
     */
    @Test
    public void testReuse() {
        School school = I.make(School.class);
        school.setName("OtoTatibana");

        Student student = I.make(Student.class);
        student.setAge(17);
        student.setFirstName("Himeko");
        student.setLastName("Kurusugawa");
        student.setSchool(school);

        List<Student> students = new ArrayList();
        students.add(student);
        school.setStudents(students);

        // result collector
        Collector collector = new Collector();

        // traverse
        ModelWalker walker = new ModelWalker(student);
        walker.addListener(collector);
        walker.traverse();

        // assert
        assertEquals(8, collector.enterNodes.size());

        // reuse
        collector.enterNodes.clear();
        walker.traverse();

        // assert
        assertEquals(8, collector.enterNodes.size());
    }

    /**
     * Traverse model. Primitive Model
     */
    @Test
    public void testTraverseModel01() {
        List<String> paths = new ArrayList();
        paths.add("age");

        ModelWalker walker = new ModelWalker(I.make(Person.class));
        Model model = walker.traverseModel(paths);
        assertNotNull(model);
        assertEquals(int.class, model.type);
    }

    /**
     * Traverse model. Non-Primitive Model
     */
    @Test
    public void testTraverseModel02() {
        List<String> paths = new ArrayList();
        paths.add("firstName");

        ModelWalker walker = new ModelWalker(I.make(Person.class));
        Model model = walker.traverseModel(paths);
        assertNotNull(model);
        assertEquals(String.class, model.type);
    }

    /**
     * Traverse model with nested path.
     */
    @Test
    public void testTraverseModel03() {
        List<String> paths = new ArrayList();
        paths.add("school");
        paths.add("name");

        ModelWalker walker = new ModelWalker(I.make(Student.class));
        Model model = walker.traverseModel(paths);
        assertNotNull(model);
        assertEquals(String.class, model.type);
    }

    /**
     * Traverse model with nonexistent property name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTraverseModel04() {
        List<String> paths = new ArrayList();
        paths.add("nonexistent");

        ModelWalker walker = new ModelWalker(I.make(Person.class));
        walker.traverseModel(paths);
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/17 14:26:57
     */
    private static class Collector implements ModelWalkListener {

        /** The store. */
        private List<Info> enterNodes = new ArrayList();

        private List<Info> leaveNodes = new ArrayList();

        /**
         * @see ezbean.model.ModelWalkListener#enterNode(ezbean.model.Model,
         *      ezbean.model.Property, java.lang.Object)
         */
        public void enterNode(Model model, Property property, Object node) {
            Info info = new Info();
            info.model = model;
            info.property = property;
            info.value = node;

            enterNodes.add(info);
        }

        /**
         * @see ezbean.model.ModelWalkListener#leaveNode(ezbean.model.Model,
         *      ezbean.model.Property, java.lang.Object)
         */
        public void leaveNode(Model model, Property property, Object propertyValue) {
            Info info = new Info();
            info.model = model;
            info.property = property;
            info.value = propertyValue;

            leaveNodes.add(info);
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/17 14:26:53
     */
    private static class Info {

        private Model model;

        private Property property;

        private Object value;

        /**
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            return "Info [" + model.name + ", " + property.name + ", " + value + "]";
        }
    }
}
