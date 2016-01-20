/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.performance;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;

import antibug.benchmark.Benchmark;
import antibug.benchmark.Benchmark.Code;
import kiss.I;
import kiss.sample.bean.Person;

/**
 * @version 2012/01/31 16:13:18
 */
public class AccessibleBenchmark {

    @Rule
    @ClassRule
    public static final Benchmark benchmark = new Benchmark();

    /**
     * Normal access.
     */
    @Test
    public void method() {
        benchmark.measure(new Code() {

            private String value = "test";

            private Person person = I.make(Person.class);

            @Override
            public Object measure() throws Throwable {
                person.setFirstName(value);
                return person;
            }
        });
    }

    /**
     * Reflection access.
     */
    @Test
    public void reflection() {
        benchmark.measure(new Code() {

            private Person person = I.make(Person.class);

            private Method method;

            private Object[] param = new Object[] {"test"};

            {
                try {
                    method = Person.class.getDeclaredMethod("setFirstName", new Class[] {String.class});
                    method.setAccessible(true);
                } catch (SecurityException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Object measure() throws Throwable {
                method.invoke(person, param);
                return person;
            }
        });
    }

    /**
     * MH access.
     */
    @Test
    public void methodHandle() {
        benchmark.measure(new Code() {

            private Person person = I.make(Person.class);

            private MethodHandle method;

            private String param = "test";

            {
                try {
                    MethodType type = MethodType.methodType(void.class, String.class);
                    method = MethodHandles.lookup().findVirtual(Person.class, "setFirstName", type);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Object measure() throws Throwable {
                method.invoke(person, param);
                return person;
            }
        });
    }

    /**
     * MH access.
     */
    @Test
    public void methodHandleExact() {
        benchmark.measure(new Code() {

            private Person person = I.make(Person.class);

            private MethodHandle method;

            private String param = "test";

            {
                try {
                    Field field = Lookup.class.getDeclaredField("IMPL_LOOKUP");
                    field.setAccessible(true);
                    Lookup lookup = (Lookup) field.get(null);

                    MethodType type = MethodType.methodType(void.class, String.class);
                    method = lookup.findVirtual(Person.class, "setFirstName", type);
                    method = method.bindTo(person);
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Object measure() throws Throwable {
                method.invoke(param);
                return person;
            }
        });
    }
}
