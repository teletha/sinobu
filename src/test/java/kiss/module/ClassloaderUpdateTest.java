/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.module;

import kiss.I;

import org.junit.Rule;
import org.junit.Test;

import antibug.PrivateModule;

/**
 * @version 2011/03/22 17:07:32
 */
public class ClassloaderUpdateTest {

    @Rule
    public static PrivateModule module = new PrivateModule(true, false);

    @Test
    public void updateClassloader() throws Exception {
        Object object1 = I.make(module.convert(Private.class));

        // reload
        module.unload();
        module.load();

        Object object2 = I.make(module.convert(Private.class));

        assert object1 != object2;
        assert object1.getClass() != object2.getClass();
        assert object2.getClass().getName().equals(object1.getClass().getName());
        assert object1.getClass().getClassLoader() != object2.getClass().getClassLoader();
    }

    /**
     * @version 2010/11/13 23:12:49
     */
    private static class Private {
    }
}
