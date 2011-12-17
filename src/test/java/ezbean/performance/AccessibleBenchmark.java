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
package ezbean.performance;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;

import org.junit.Test;

import ezbean.I;
import ezbean.model.Model;
import ezbean.model.Property;
import ezbean.sample.bean.Person;
import ezunit.AbstractMicroBenchmarkTest;

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
    public void method() {
        benchmark(new Callable() {

            private String value = "test";

            private Person person = I.make(Person.class);

            /**
             * @see java.util.concurrent.Callable#call()
             */
            public Object call() throws Exception {
                person.setFirstName(value);
                return person;
            }
        });
    }

    /**
     * Direct access.
     */
    @Test
    public void set() {
        benchmark(new Callable() {

            private Person person = I.make(Person.class);

            private Model model = Model.load(Person.class);

            private Property property = model.getProperty("firstName");

            private String value = "test";

            /**
             * @see java.util.concurrent.Callable#call()
             */
            public Object call() throws Exception {
                model.set(person, property, value);
                return person;
            }
        });
    }

    /**
     * Reflection access.
     */
    @Test
    public void reflection() {
        benchmark(new Callable() {

            private Person person = I.make(Person.class);

            private Method method;

            private Object[] param = new Object[] {"test"};

            {
                try {
                    method = person.getClass().getDeclaredMethod("setFirstName", new Class[] {String.class});
                    method.setAccessible(true);
                } catch (SecurityException e) {
                    throw new RuntimeException(e);
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
            }

            /**
             * @see java.util.concurrent.Callable#call()
             */
            public Object call() throws Exception {
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
        benchmark(new Callable() {

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
             * @see java.util.concurrent.Callable#call()
             */
            public Object call() throws Exception {
                try {
                    method.invokeExact(person, param);
                    return person;
                } catch (Throwable e) {
                    throw I.quiet(e);
                }
            }
        });
    }
}
