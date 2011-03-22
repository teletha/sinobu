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
package ezbean;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ezbean.sample.bean.ArrayBean;
import ezbean.sample.bean.GenericBean;
import ezbean.sample.bean.Person;
import ezbean.sample.bean.Primitive;
import ezbean.sample.bean.Student;
import ezbean.sample.dependency.DependenciedBean;

/**
 * @version 2011/03/22 16:45:42
 */
public class EzbeanTraceTest {

    /** The private field reference to test. */
    private static final ThreadSpecific<Deque<List>> tracers;

    // initialize
    static {
        try {
            Field field = I.class.getDeclaredField("tracers");
            field.setAccessible(true);

            tracers = (ThreadSpecific<Deque<List>>) field.get(null);
        } catch (Exception e) {
            // If this exception will be thrown, it is bug of this program. So we must rethrow the
            // wrapped error in here.
            throw new Error(e);
        }
    }

    /**
     * Clear context;
     */
    @Before
    @After
    public void clear() {
        Deque tracer = tracers.resolve();

        if (tracer != null) {
            tracer.clear();
        }
    }

    /**
     * Test single property path.
     */
    @Test
    public void testTrace01() {
        Person person = I.make(Person.class);
        I.mock(person).getAge();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert person == tracer.getFirst().get(0);
        assert "age" == tracer.getFirst().get(1);
    }

    /**
     * Test multiple property paths.
     */
    @Test
    public void testTrace02() {
        Student student = I.make(Student.class);
        I.mock(student).getSchool().getName();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert student == tracer.getFirst().get(0);
        assert "school" == tracer.getFirst().get(1);
        assert "name" == tracer.getFirst().get(2);
    }

    @Test
    public void testTraceBoolean() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).isBoolean();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert primitive == tracer.getFirst().get(0);
        assert "boolean" == tracer.getFirst().get(1);
    }

    @Test
    public void testTraceInt() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getInt();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert primitive == tracer.getFirst().get(0);
        assert "int" == tracer.getFirst().get(1);
    }

    @Test
    public void testTraceLong() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getLong();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert primitive == tracer.getFirst().get(0);
        assert "long" == tracer.getFirst().get(1);
    }

    @Test
    public void testTraceFloat() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getFloat();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert primitive == tracer.getFirst().get(0);
        assert "float" == tracer.getFirst().get(1);
    }

    @Test
    public void testTraceDouble() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getDouble();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert primitive == tracer.getFirst().get(0);
        assert "double" == tracer.getFirst().get(1);
    }

    @Test
    public void testTraceChar() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getChar();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert primitive == tracer.getFirst().get(0);
        assert "char" == tracer.getFirst().get(1);
    }

    @Test
    public void testTraceByte() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getByte();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert primitive == tracer.getFirst().get(0);
        assert "byte" == tracer.getFirst().get(1);
    }

    @Test
    public void testTraceShort() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getShort();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert primitive == tracer.getFirst().get(0);
        assert "short" == tracer.getFirst().get(1);
    }

    @Test
    public void testArray() {
        ArrayBean array = I.make(ArrayBean.class);
        I.mock(array).getObjects();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert array == tracer.getFirst().get(0);
        assert "objects" == tracer.getFirst().get(1);
    }

    @Test
    public void testSuperMethod() {
        Subclass bean = I.make(Subclass.class);
        I.mock(bean).getFirstName();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert bean == tracer.getFirst().get(0);
        assert "firstName" == tracer.getFirst().get(1);
    }

    /**
     * @version 2009/07/15 16:59:42
     */
    protected static class Subclass extends Person {
    }

    @Test
    public void testGenericMethod() {
        Generic bean = I.make(Generic.class);
        I.mock(bean).getGeneric();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert bean == tracer.getFirst().get(0);
        assert "generic" == tracer.getFirst().get(1);
    }

    /**
     * @version 2009/07/15 16:59:42
     */
    public static class Generic extends GenericBean<String> {
    }

    /**
     * Test multiple property paths.
     */
    @Test
    public void testTraceWithDependency() {
        DependenciedBean bean = I.make(DependenciedBean.class);
        I.mock(bean).getName();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert bean == tracer.getFirst().get(0);
        assert "name" == tracer.getFirst().get(1);
    }

    @Test
    public void testDontCallActualMethod() {
        DontCall call = I.make(DontCall.class);
        DontCall mock = I.mock(call);
        assert !call.called;
        assert !mock.called;

        // call at mock object
        mock.getName();

        Deque<List> tracer = tracers.resolve();

        assert 1 == tracer.size();
        assert call == tracer.getFirst().get(0);
        assert "name" == tracer.getFirst().get(1);
        assert !call.called;
        assert !mock.called;
    }

    /**
     * @version 2009/07/02 1:33:26
     */
    protected static class DontCall {

        private boolean called = false;

        private String name;

        /**
         * Get the name property of this {@link EzbeanTraceTest.DontCall}.
         * 
         * @return The name property.
         */
        public String getName() {
            called = true;
            return name;
        }

        /**
         * Set the name property of this {@link EzbeanTraceTest.DontCall}.
         * 
         * @param name The name value to set.
         */
        public void setName(String name) {
            this.name = name;
        }
    }

    /**
     * Test <code>null</code> input.
     */
    @Test(expected = NullPointerException.class)
    public void testNull() {
        I.mock(null);
    }

    /**
     * Test {@link String} input.
     */
    @Test(expected = NullPointerException.class)
    public void testString() {
        I.mock("string");
    }
}
