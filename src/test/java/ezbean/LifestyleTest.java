/*
 * Copyright (C) 2010 Nameless Production Committee.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ezbean;

import static org.junit.Assert.*;

import org.junit.Rule;
import org.junit.Test;

import ezbean.sample.bean.Person;
import ezbean.unit.ClassModule;

/**
 * @version 2010/01/15 18:51:51
 */
public class LifestyleTest {

    @Rule
    public static final ClassModule module = new ClassModule();

    @Test
    public void customLifestyle() {
        assertSame(WithoutLifestyle.object, I.make(Without.class));
    }

    @Test
    public void unload() {
        assertSame(WithoutLifestyle.object, I.make(Without.class));
        assertSame(WithoutLifestyle.object, I.make(Without.class));

        // unload module
        I.unload(module.moduleFile);

        Without object = I.make(Without.class);
        assertNotSame(WithoutLifestyle.object, object);
        assertNotSame(object, I.make(Without.class));
    }

    @Test
    public void override() {
        With object = I.make(With.class);
        assertNotSame(object, I.make(With.class));

        // unload module
        I.unload(module.moduleFile);

        object = I.make(With.class);
        assertSame(object, I.make(With.class));
    }

    @Test
    public void extendPrototype() {
        Person person = I.make(Person.class);
        assertEquals("default", person.getFirstName());
    }

    /**
     * <p>
     * External class without {@link Manageable} annotation.
     * </p>
     * 
     * @version 2010/01/15 18:55:45
     */
    private static class Without {
    }

    /**
     * Custom lifestyle.
     * 
     * @version 2010/01/15 18:54:13
     */
    private static class WithoutLifestyle implements Lifestyle<Without> {

        private static final Without object = new Without();

        /**
         * @see ezbean.Lifestyle#resolve()
         */
        public Without resolve() {
            return object;
        }
    }

    /**
     * <p>
     * External class with {@link Manageable} annotation.
     * </p>
     * 
     * @version 2010/01/15 18:55:45
     */
    @Manageable(lifestyle = Singleton.class)
    private static class With {
    }

    /**
     * Custom lifestyle.
     * 
     * @version 2010/01/15 18:54:13
     */
    @SuppressWarnings("unused")
    private static class WithLifestyle implements Lifestyle<With> {

        /**
         * @see ezbean.Lifestyle#resolve()
         */
        public With resolve() {
            return new With();
        }
    }

    /**
     * @version 2010/01/16 13:10:29
     */
    @SuppressWarnings("unused")
    private static class PersonLifestyle extends Prototype<Person> {

        public PersonLifestyle() {
            super(Person.class);
        }

        /**
         * @see ezbean.Prototype#resolve()
         */
        @Override
        public Person resolve() {
            Person person = super.resolve();

            person.setFirstName("default");

            return person;
        }
    }
}
