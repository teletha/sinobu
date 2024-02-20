/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.instantiation;

import java.util.List;

import org.junit.jupiter.api.Test;

import kiss.I;
import kiss.Lifestyle;
import kiss.Managed;
import kiss.Singleton;
import kiss.sample.bean.Person;

public class ConstructorInjectionTest {

    @Test
    @SuppressWarnings("unused")
    public void primitiveIntInjection() {
        class Injection {
            private int value;

            Injection(int injectable) {
                this.value = injectable;
            }
        }

        assert I.make(Injection.class).value == 0;
    }

    @Test
    @SuppressWarnings("unused")
    public void primitiveLongInjection() {
        class Injection {
            private long value;

            Injection(long injectable) {
                this.value = injectable;
            }
        }

        assert I.make(Injection.class).value == 0L;
    }

    @Test
    @SuppressWarnings("unused")
    public void primitiveFloatInjection() {
        class Injection {
            private float value;

            Injection(float injectable) {
                this.value = injectable;
            }
        }

        assert I.make(Injection.class).value == 0f;
    }

    @Test
    @SuppressWarnings("unused")
    public void primitiveDoubleInjection() {
        class Injection {
            private double value;

            Injection(double injectable) {
                this.value = injectable;
            }
        }

        assert I.make(Injection.class).value == 0d;
    }

    @Test
    @SuppressWarnings("unused")
    public void primitiveBooleanInjection() {
        class Injection {
            private boolean value;

            Injection(boolean injectable) {
                this.value = injectable;
            }
        }

        assert I.make(Injection.class).value == false;
    }

    @Test
    @SuppressWarnings("unused")
    public void objectInjection() {
        class Injectable {
        }

        class Injection {
            private Injectable value;

            Injection(Injectable injectable) {
                this.value = injectable;
            }
        }

        assert I.make(Injection.class).value != null;
    }

    @Test
    @SuppressWarnings("unused")
    public void objectLazyInjection() {
        class Injection {
            private Lifestyle<Person> lazy;

            Injection(Lifestyle<Person> injectable, List<String> item, int value) {
                this.lazy = injectable;
            }
        }

        assert I.make(Injection.class).lazy.get() != null;
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
    public static class Injected {
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
    public static class CircularLifestyleA {

        private Lifestyle<CircularLifestyleB> other;

        private CircularLifestyleA(Lifestyle<CircularLifestyleB> other) {
            this.other = other;
        }
    }

    /**
     * @version 2010/02/17 13:53:26
     */
    public static class CircularLifestyleB {

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