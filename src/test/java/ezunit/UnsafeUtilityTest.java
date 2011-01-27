/**
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
package ezunit;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * @version 2010/02/12 19:59:11
 */
public class UnsafeUtilityTest {

    @Test
    public void instantiate() throws Exception {
        assertEquals(null, new Root().clazz);
        assertEquals(UnsafeUtilityTest.class, new Parent().clazz);
        assertEquals(UnsafeUtilityTest.class, new Child().clazz);
    }

    @Test
    public void instantiateFromStaticMethod() throws Exception {
        assertEquals(Root.class, Root.speculteParent());
        assertEquals(Root.class, Root.speculteChild());
        assertEquals(Parent.class, Parent.speculteParent());
        assertEquals(Parent.class, Parent.speculteChild());
        assertEquals(Child.class, Child.speculteParent());
        assertEquals(Child.class, Child.speculteChild());
    }

    @Test
    public void instantiateFromStaticInitializer() throws Exception {
        assertEquals(Root.class, Root.fromStatic);
        assertEquals(Parent.class, Parent.fromStatic);
        assertEquals(Child.class, Child.fromStatic);
    }

    /**
     * @version 2010/02/12 20:00:00
     */
    private static class Root {

        private static Class fromStatic;

        static {
            fromStatic = new Parent().clazz;
        }

        protected Class clazz;

        private static Class speculteParent() {
            return new Parent().clazz;
        }

        private static Class speculteChild() {
            return new Child().clazz;
        }
    }

    /**
     * @version 2010/02/12 20:00:46
     */
    private static class Parent extends Root {

        private static Class fromStatic;

        static {
            fromStatic = new Parent().clazz;
        }

        public Parent() {
            clazz = UnsafeUtility.speculateInstantiator();
        }

        private static Class speculteParent() {
            return new Parent().clazz;
        }

        private static Class speculteChild() {
            return new Child().clazz;
        }
    }

    /**
     * @version 2010/02/12 20:00:46
     */
    private static class Child extends Parent {

        private static Class fromStatic;

        static {
            fromStatic = new Parent().clazz;
        }

        private static Class speculteParent() {
            return new Parent().clazz;
        }

        private static Class speculteChild() {
            return new Child().clazz;
        }
    }
}
