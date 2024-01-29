/*
 * Copyright (C) 2024 The SINOBU Development Team
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          https://opensource.org/licenses/MIT
 */
package kiss.core;

import java.util.HashMap;
import java.util.Set;

import org.junit.jupiter.api.Test;

import kiss.Model;

class CollectTypesTest {

    @Test
    void collect() {
        Set<Class> classes = Model.collectTypes(ExtendClass.class);
        assert 7 == classes.size();
        assert classes.contains(ExtendClass.class);
    }

    @Test
    void nullParameter() {
        Set<Class> classes = Model.collectTypes(null);
        assert 0 == classes.size();
    }

    private static class ExtendClass extends HashMap {

        private static final long serialVersionUID = -5962628342667538716L;
    }
}