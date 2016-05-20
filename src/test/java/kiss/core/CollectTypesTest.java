/*
 * Copyright (C) 2016 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.core;

import java.util.ArrayList;
import java.util.Set;

import org.junit.Test;

import kiss.model.Model;

/**
 * @version 2016/05/04 2:14:41
 */
public class CollectTypesTest {

    @Test
    public void collect() {
        Set<Class> classes = Model.collectTypes(ExtendClass.class);
        assert 11 == classes.size();
        assert classes.contains(ExtendClass.class);
    }

    @Test
    public void nullParameter() {
        Set<Class> classes = Model.collectTypes(null);
        assert 0 == classes.size();
    }

    /**
     * @version 2016/05/04 2:15:08
     */
    private static class ExtendClass extends ArrayList {

        private static final long serialVersionUID = -5962628342667538716L;
    }
}
