/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package ezbean.performance;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Ignore;
import org.junit.Test;

import ezbean.I;
import ezbean.sample.bean.Person;
import ezunit.AbstractMicroBenchmarkTest;
import ezunit.BenchmarkCode;

/**
 * DOCUMENT.
 * 
 * @version 2008/11/05 15:45:55
 */
public class AccessibleBenchmark extends AbstractMicroBenchmarkTest {

    /**
     * Normal access.
     */
    @Test
    @Ignore
    public void method() {
        benchmark(new BenchmarkCode() {

            private String value = "test";

            private Person person = I.make(Person.class);

            /**
             * {@inheritDoc}
             */
            public Object call() throws Throwable {
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
        benchmark(new BenchmarkCode() {

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

            /**
             * {@inheritDoc}
             */
            public Object call() throws Throwable {
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
        benchmark(new BenchmarkCode() {

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

            /**
             * {@inheritDoc}
             */
            public Object call() throws Throwable {
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
        benchmark(new BenchmarkCode() {

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

            /**
             * {@inheritDoc}
             */
            public Object call() throws Throwable {
                method.invoke(param);
                return person;
            }
        });
    }
}
