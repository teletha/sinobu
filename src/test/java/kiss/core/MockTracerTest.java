/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import java.lang.reflect.Field;
import java.util.Deque;
import java.util.List;

import kiss.I;
import kiss.ThreadSpecific;
import kiss.sample.bean.ArrayBean;
import kiss.sample.bean.GenericBean;
import kiss.sample.bean.Person;
import kiss.sample.bean.Primitive;
import kiss.sample.bean.Student;
import kiss.sample.dependency.DependenciedBean;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @version 2014/01/23 14:21:15
 */
public class MockTracerTest {

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
        Deque tracer = tracers.get();

        if (tracer != null) {
            tracer.clear();
        }
    }

    @Test
    public void path() {
        Person person = I.make(Person.class);
        I.mock(person).getAge();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == person;
        assert tracer.getFirst().get(1).equals("age");
    }

    @Test
    public void multiplePath() {
        Student student = I.make(Student.class);
        I.mock(student).getSchool().getName();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == student;
        assert tracer.getFirst().get(1).equals("school");
        assert tracer.getFirst().get(2).equals("name");
    }

    @Test
    public void Boolean() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).isBoolean();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == primitive;
        assert tracer.getFirst().get(1).equals("boolean");
    }

    @Test
    public void Int() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getInt();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == primitive;
        assert tracer.getFirst().get(1).equals("int");
    }

    @Test
    public void Long() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getLong();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == primitive;
        assert tracer.getFirst().get(1).equals("long");
    }

    @Test
    public void Float() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getFloat();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == primitive;
        assert tracer.getFirst().get(1).equals("float");
    }

    @Test
    public void Double() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getDouble();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == primitive;
        assert tracer.getFirst().get(1).equals("double");
    }

    @Test
    public void Char() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getChar();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == primitive;
        assert tracer.getFirst().get(1).equals("char");
    }

    @Test
    public void Short() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getShort();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == primitive;
        assert tracer.getFirst().get(1).equals("short");
    }

    @Test
    public void Byte() {
        Primitive primitive = I.make(Primitive.class);
        I.mock(primitive).getByte();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == primitive;
        assert tracer.getFirst().get(1).equals("byte");
    }

    @Test
    public void Array() {
        ArrayBean array = I.make(ArrayBean.class);
        I.mock(array).getObjects();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == array;
        assert tracer.getFirst().get(1).equals("objects");
    }

    @Test
    public void superMethod() {
        Subclass bean = I.make(Subclass.class);
        I.mock(bean).getFirstName();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == bean;
        assert tracer.getFirst().get(1).equals("firstName");
    }

    /**
     * @version 2014/01/23 14:58:40
     */
    protected static class Subclass extends Person {
    }

    @Test
    public void genericMethod() {
        Generic bean = I.make(Generic.class);
        I.mock(bean).getGeneric();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == bean;
        assert tracer.getFirst().get(1).equals("generic");
    }

    /**
     * @version 2014/01/23 14:59:50
     */
    protected static class Generic extends GenericBean<String> {
    }

    @Test
    public void dependency() {
        DependenciedBean bean = I.make(DependenciedBean.class);
        I.mock(bean).getName();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == bean;
        assert tracer.getFirst().get(1).equals("name");
    }

    @Test
    public void dontCallActualMethod() {
        DontCall call = I.make(DontCall.class);
        DontCall mock = I.mock(call);
        assert call.called == false;
        assert mock.called == false;

        // call at mock object
        mock.getName();

        Deque<List> tracer = tracers.get();

        assert tracer.size() == 1;
        assert tracer.getFirst().get(0) == call;
        assert tracer.getFirst().get(1).equals("name");
        assert call.called == false;
        assert mock.called == false;
    }

    /**
     * @version 2014/01/23 15:19:57
     */
    protected static class DontCall {

        private boolean called = false;

        private String name;

        /**
         * Get the name property of this {@link EasyBeanTraceTest.DontCall}.
         * 
         * @return The name property.
         */
        public String getName() {
            called = true;
            return name;
        }

        /**
         * Set the name property of this {@link EasyBeanTraceTest.DontCall}.
         * 
         * @param name The name value to set.
         */
        public void setName(String name) {
            this.name = name;
        }
    }

    @Test(expected = NullPointerException.class)
    public void nullParam() {
        I.mock(null);
    }

    /**
     * Test {@link String} input.
     */
    @Test(expected = NullPointerException.class)
    public void stringParam() {
        I.mock("string");
    }
}
