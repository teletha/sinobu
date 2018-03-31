/*
 * Copyright (C) 2018 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import java.lang.reflect.Constructor;
import java.util.HashMap;

import org.junit.jupiter.api.Test;

import kiss.model.Model;

/**
 * @version 2016/05/15 9:53:41
 */
public class CollectConstructorsTest {

    @Test
    public void none() {
        Constructor[] constructors = Model.collectConstructors(NoneConstructor.class);
        assert constructors != null;
        assert constructors.length == 1;
    }

    /**
     * @version 2016/05/15 9:54:10
     */
    private static class NoneConstructor {
    }

    @Test
    public void single() {
        Constructor[] constructors = Model.collectConstructors(OneConstructor.class);
        assert constructors != null;
        assert constructors.length == 1;
    }

    /**
     * @version 2016/05/15 9:56:02
     */
    private static class OneConstructor {

        private OneConstructor(int i) {
        }
    }

    @Test
    public void two() {
        Constructor[] constructors = Model.collectConstructors(TwoConstructor.class);
        assert constructors != null;
        assert constructors.length == 2;
        assert constructors[0].getParameterCount() == 1;
        assert constructors[1].getParameterCount() == 2;
    }

    /**
     * @version 2016/05/15 9:56:47
     */
    private static class TwoConstructor {

        private TwoConstructor(int i, String name) {
        }

        private TwoConstructor(int i) {
        }
    }

    @Test
    public void multi() {
        Constructor[] constructors = Model.collectConstructors(ThreeConstructor.class);
        assert constructors != null;
        assert constructors.length == 3;
        assert constructors[0].getParameterCount() == 1;
        assert constructors[1].getParameterCount() == 2;
        assert constructors[2].getParameterCount() == 3;
    }

    /**
     * @version 2016/05/15 9:56:47
     */
    private static class ThreeConstructor {

        private ThreeConstructor(int i, String name) {
        }

        private ThreeConstructor(int i) {
        }

        private ThreeConstructor(int i, String name, boolean log) {
        }
    }

    @Test
    public void existing() {
        Constructor[] constructors = Model.collectConstructors(HashMap.class);
        assert constructors != null;
        assert constructors.length == 4;
        assert constructors[0].getParameterCount() == 0;
        assert constructors[1].getParameterCount() == 1;
    }
}
