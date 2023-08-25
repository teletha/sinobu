/*
 * Copyright (C) 2023 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import kiss.Managed;
import kiss.Model;

class CollectConstructorsTest {

    @Test
    void none() {
        Constructor[] constructors = Model.collectConstructors(NoneConstructor.class);
        assert constructors != null;
        assert constructors.length == 1;
    }

    private static class NoneConstructor {
    }

    @Test
    void single() {
        Constructor[] constructors = Model.collectConstructors(OneConstructor.class);
        assert constructors != null;
        assert constructors.length == 1;
    }

    private static class OneConstructor {

        private OneConstructor(int i) {
        }
    }

    @Test
    void two() {
        Constructor[] constructors = Model.collectConstructors(TwoConstructor.class);
        assert constructors != null;
        assert constructors.length == 2;
        assert constructors[0].getParameterCount() == 1;
        assert constructors[1].getParameterCount() == 2;
    }

    private static class TwoConstructor {

        private TwoConstructor(int i, String name) {
        }

        private TwoConstructor(int i) {
        }
    }

    @Test
    void multi() {
        Constructor[] constructors = Model.collectConstructors(ThreeConstructor.class);
        assert constructors != null;
        assert constructors.length == 3;
        assert constructors[0].getParameterCount() == 1;
        assert constructors[1].getParameterCount() == 2;
        assert constructors[2].getParameterCount() == 3;
    }

    private static class ThreeConstructor {

        private ThreeConstructor(int i, String name) {
        }

        private ThreeConstructor(int i) {
        }

        private ThreeConstructor(int i, String name, boolean log) {
        }
    }

    @Test
    void existing() {
        Constructor[] constructors = Model.collectConstructors(HashMap.class);
        assert constructors != null;
        assert constructors.length == 4;
        assert constructors[0].getParameterCount() == 0;
        assert constructors[1].getParameterCount() == 1;
    }

    @Test
    void highPriorityWithManagedAnnotation() {
        Constructor[] constructors = Model.collectConstructors(WithManaged.class);
        assert constructors != null;
        assert constructors.length == 2;
        assert constructors[0].getParameterCount() == 2;
        assert constructors[1].getParameterCount() == 0;
    }

    private static class WithManaged {

        private WithManaged() {
        }

        @Managed
        private WithManaged(String name, int priority) {
        }
    }

    @Test
    void highPriorityWithInjectAnnotation() {
        Constructor[] constructors = Model.collectConstructors(WithInject.class);
        assert constructors != null;
        assert constructors.length == 2;
        assert constructors[0].getParameterCount() == 2;
        assert constructors[1].getParameterCount() == 0;
    }

    @Target(ElementType.CONSTRUCTOR)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface Inject {
    }

    private static class WithInject {

        private WithInject() {
        }

        @Inject
        private WithInject(String name, int priority) {
        }
    }
}