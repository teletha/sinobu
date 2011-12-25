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
package ezbean.model;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ezbean.I;
import ezbean.sample.bean.Person;
import ezbean.sample.bean.School;
import ezbean.sample.bean.Student;

/**
 * @version 2011/12/26 0:11:11
 */
public class ModelWalkerTest {

    /**
     * Test simple traverse.
     */
    @Test
    public void traverse1() throws Exception {
        // root
        Person person = I.make(Person.class);

        // result collector
        Collector collector = new Collector();

        // traverse
        collector.walk(person);

        // assert
        assert 2 == collector.enterNodes.size();
        assert 2 == collector.leaveNodes.size();
    }

    /**
     * Test simple traverse.
     */
    @Test
    public void traverse2() throws Exception {
        // root
        Person person = I.make(Person.class);
        person.setFirstName("Mizuho");
        person.setLastName("Miyanokoji");
        person.setAge(18);

        // result collector
        Collector collector = new Collector();

        // traverse
        collector.walk(person);

        // assert
        assert 4 == collector.enterNodes.size();
        assert 4 == collector.leaveNodes.size();

        person = (Person) collector.enterNodes.get(0).value;
        assert 18 == person.getAge();
        assert "Mizuho" == person.getFirstName();
        assert "Miyanokoji" == person.getLastName();
    }

    /**
     * Reuse {@link ModelWalker} with cyclick nodes.
     */
    @Test
    public void reusablility() {
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
        collector.walk(student);

        // assert
        assert 8 == collector.enterNodes.size();

        // reuse
        collector.enterNodes.clear();
        collector.walk(student);

        // assert
        assert 8 == collector.enterNodes.size();
    }

    /**
     * @version 2010/01/10 8:34:35
     */
    private static class Collector extends ModelWalker {

        /** The store. */
        private List<Info> enterNodes = new ArrayList();

        private List<Info> leaveNodes = new ArrayList();

        /**
         * @see ezbean.model.ModelWalker#enter(ezbean.model.Model, ezbean.model.Property,
         *      java.lang.Object)
         */
        protected void enter(Model model, Property property, Object node) {
            Info info = new Info();
            info.model = model;
            info.property = property;
            info.value = node;

            enterNodes.add(info);
        }

        /**
         * @see ezbean.model.ModelWalker#leave(ezbean.model.Model, ezbean.model.Property,
         *      java.lang.Object)
         */
        protected void leave(Model model, Property property, Object propertyValue) {
            Info info = new Info();
            info.model = model;
            info.property = property;
            info.value = propertyValue;

            leaveNodes.add(info);
        }
    }

    /**
     * @version 2010/01/10 8:34:47
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
