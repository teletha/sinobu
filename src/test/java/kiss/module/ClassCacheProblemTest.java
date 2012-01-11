/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss.module;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import kiss.I;
import kiss.model.Model;
import kiss.module.external.ExtendedClass1;
import kiss.module.external.SingletonClass;
import ezunit.PrivateModule;

/**
 * @version 2011/03/22 17:08:49
 */
public class ClassCacheProblemTest {

    @Rule
    public static PrivateModule module = new PrivateModule("external", true, false);

    /**
     * Cached prototype class's identity check.
     */
    @Test
    public void testClassCacheForPrototype() throws Exception {
        Model model1 = Model.load(module.convert(ExtendedClass1.class));
        assert model1 != null;

        Class class1 = model1.type;
        assert class1 != null;

        ClassLoader loader1 = class1.getClassLoader();
        assert loader1 != null;

        Object object1 = I.make(class1);
        assert object1 != null;

        // reload
        module.load();

        Model model2 = Model.load(module.convert(ExtendedClass1.class));
        assert model2 != null;

        Class class2 = model2.type;
        assert class2 != null;

        ClassLoader loader2 = class2.getClassLoader();
        assert loader2 != null;

        Object object2 = I.make(class2);
        assert object2 != null;

        // check
        assert model1 != model2;
        assert class1 != class2;
        assert loader1 != loader2;
        assert object1 != object2;
    }

    /**
     * Cached singleton class's identity check.
     */
    @Test
    public void testClassCacheForSingleton() {
        Model model1 = Model.load(module.convert(SingletonClass.class));
        assert model1 != null;

        Class class1 = model1.type;
        assert class1 != null;

        Object object1a = I.make(class1);
        assert object1a != null;

        Object object1b = I.make(class1);
        assert object1b != null;

        // check singleton
        assert object1b.equals(object1a);

        // reload
        module.load();

        Model model2 = Model.load(module.convert(SingletonClass.class));
        assert model2 != null;

        Class class2 = model2.type;
        assert class2 != null;

        Object object2a = I.make(class2);
        assert object2a != null;

        Object object2b = I.make(class2);
        assert object2b != null;

        // check singleton
        assert object2b.equals(object2a);

        // check old class
        Object object3 = I.make(class1);
        assert object1a != object3;
        assert object2a != object3;
    }

    /**
     * Class as reference key will be removed automatically.
     */
    @Test
    public void testClassCacheInMapKeyReference() {
        Model model = Model.load(module.convert(ExtendedClass1.class));
        assert model != null;

        Class clazz = model.type;
        assert clazz != null;

        // create module aware map
        Map<Class, Object> cache = I.aware(new HashMap());
        cache.put(clazz, "1");
        assert cache.containsKey(clazz);

        // unload module
        module.unload();
        assert !cache.containsKey(clazz);
    }
}
