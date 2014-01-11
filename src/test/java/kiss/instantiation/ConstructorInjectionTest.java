/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.instantiation;

import org.junit.Test;

import kiss.I;
import kiss.Lifestyle;
import kiss.Manageable;
import kiss.Singleton;

/**
 * @version 2011/03/22 16:54:08
 */
public class ConstructorInjectionTest {

    /**
     * Test constructor injection.
     */
    @Test
    public void constructorInjection() {
        ConstructorInjection test = I.make(ConstructorInjection.class);
        assert test != null;
        assert test.injected != null;
    }

    /**
     * Test singleton injection.
     */
    @Test
    public void singletonInjection() {
        ConstructorSingletonInjection test1 = I.make(ConstructorSingletonInjection.class);
        assert test1 != null;
        assert test1.injected != null;

        ConstructorSingletonInjection test2 = I.make(ConstructorSingletonInjection.class);
        assert test2 != null;
        assert test2.injected != null;

        assert test1 != test2;
        assert test1.injected == test2.injected;
    }

    /**
     * Test too many constructors.
     */
    @Test
    public void tooManyConstructors() {
        TooManyConstructors test = I.make(TooManyConstructors.class);
        assert test != null;
        assert test.injected == null;
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
        assert circularA.other != null;

        CircularLifestyleB circularB = I.make(CircularLifestyleB.class);
        assert circularB.other != null;
    }

    /**
     * Circular dependency.
     */
    @Test
    public void circularDependenciesWithProviderMix() {
        CircularMixA circularA = I.make(CircularMixA.class);
        assert circularA.other != null;

        CircularMixB circularB = I.make(CircularMixB.class);
        assert circularB.other != null;
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
