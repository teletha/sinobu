/*
 * Copyright (C) 2019 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.instantiation;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Lifestyle;
import kiss.Managed;
import kiss.Singleton;

/**
 * @version 2017/04/21 21:09:43
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
         */
        private TooManyConstructors() {
        }

        /**
         * Create TooManyConstructors instance.
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
    @Managed(value = Singleton.class)
    private static class SingletonInjected {
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
