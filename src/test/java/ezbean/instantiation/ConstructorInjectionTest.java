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
package ezbean.instantiation;

import static org.junit.Assert.*;

import org.junit.Test;

import ezbean.I;
import ezbean.Lifestyle;
import ezbean.Manageable;
import ezbean.Singleton;

/**
 * @version 2010/02/18 9:35:35
 */
public class ConstructorInjectionTest {

    /**
     * Test constructor injection.
     */
    @Test
    public void constructorInjection() {
        ConstructorInjection test = I.make(ConstructorInjection.class);
        assertNotNull(test);
        assertNotNull(test.injected);
    }

    /**
     * Test singleton injection.
     */
    @Test
    public void singletonInjection() {
        ConstructorSingletonInjection test1 = I.make(ConstructorSingletonInjection.class);
        assertNotNull(test1);
        assertNotNull(test1.injected);

        ConstructorSingletonInjection test2 = I.make(ConstructorSingletonInjection.class);
        assertNotNull(test2);
        assertNotNull(test2.injected);

        assertNotSame(test1, test2);
        assertEquals(test1.injected, test2.injected);
    }

    /**
     * Test too many constructors.
     */
    @Test
    public void tooManyConstructors() {
        TooManyConstructors test = I.make(TooManyConstructors.class);
        assertNotNull(test);
        assertNull(test.injected);
    }

    /**
     * @version 2010/02/09 20:38:46
     */
    private static class ConstructorInjection {

        /** The dependency, */
        private Injected injected;

        /**
         * Create ConstructorInjection instance.
         * 
         * @param injected
         */
        private ConstructorInjection(Injected injected) {
            this.injected = injected;
        }
    }

    /**
     * @version 2010/02/09 20:38:50
     */
    private static class ConstructorSingletonInjection {

        /** The dependency, */
        private SingletonInjected injected;

        /**
         * Create ConstructorSingletonInjection instance.
         * 
         * @param injected
         */
        private ConstructorSingletonInjection(SingletonInjected injected) {
            this.injected = injected;
        }
    }

    /**
     * @version 2010/02/09 20:38:53
     */
    private static class TooManyConstructors {

        /** The dependency, */
        private Injected injected;

        /**
         * Create TooManyConstructors instance.
         * 
         * @param invalid
         */
        private TooManyConstructors() {
        }

        /**
         * Create TooManyConstructors instance.
         * 
         * @param injected
         */
        private TooManyConstructors(Injected injected) {
            this.injected = injected;
        }
    }

    /**
     * @version 2010/02/09 20:38:57
     */
    private static class Injected {
    }

    /**
     * @version 2010/02/09 20:39:04
     */
    @Manageable(lifestyle = Singleton.class)
    private static class SingletonInjected {
    }

    /**
     * Circular dependency.
     */
    @Test(expected = ClassCircularityError.class)
    public void circularDependenciesFromA() {
        I.make(CircularA.class);
    }

    /**
     * Circular dependency.
     */
    @Test(expected = ClassCircularityError.class)
    public void circularDependenciesFromB() {
        I.make(CircularB.class);
    }

    /**
     * Circular dependency.
     */
    @Test
    public void circularDependenciesWithProvider() {
        CircularLifestyleA circularA = I.make(CircularLifestyleA.class);
        assertNotNull(circularA.other);

        CircularLifestyleB circularB = I.make(CircularLifestyleB.class);
        assertNotNull(circularB.other);
    }

    /**
     * Circular dependency.
     */
    @Test
    public void circularDependenciesWithProviderMix() {
        CircularMixA circularA = I.make(CircularMixA.class);
        assertNotNull(circularA.other);

        CircularMixB circularB = I.make(CircularMixB.class);
        assertNotNull(circularB.other);
    }

    /**
     * @version 2010/02/17 15:55:29
     */
    private static class CircularA {

        private CircularA(CircularB circularB) {
        }
    }

    /**
     * @version 2010/02/17 15:55:17
     */
    private static class CircularB {

        private CircularB(CircularA circularA) {
        }
    }

    /**
     * @version 2010/02/17 13:53:13
     */
    private static class CircularLifestyleA {

        private Lifestyle<CircularLifestyleB> other;

        private CircularLifestyleA(Lifestyle<CircularLifestyleB> other) {
            this.other = other;
        }
    }

    /**
     * @version 2010/02/17 13:53:26
     */
    private static class CircularLifestyleB {

        private Lifestyle<CircularLifestyleA> other;

        private CircularLifestyleB(Lifestyle<CircularLifestyleA> other) {
            this.other = other;
        }
    }

    /**
     * @version 2010/02/17 14:05:28
     */
    private static class CircularMixA {

        private CircularMixB other;

        private CircularMixA(CircularMixB other) {
            this.other = other;
        }
    }

    /**
     * @version 2010/02/17 14:05:39
     */
    private static class CircularMixB {

        private Lifestyle<CircularMixA> other;

        private CircularMixB(Lifestyle<CircularMixA> other) {
            this.other = other;
        }
    }
}
