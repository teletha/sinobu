/*
 * Copyright (C) 2010 Nameless Production Committee.
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
package ezbean.module;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;

import ezbean.I;
import ezbean.model.Model;

/**
 * @version 2009/12/23 13:21:17
 */
public class ClassCacheProblemTest {

    @Rule
    public static ModuleTestRule registry = new ModuleTestRule();

    /**
     * Cached prototype class's identity check.
     */
    @Test
    public void testClassCacheForPrototype() {
        File moduleFile = registry.dir;

        registry.load(moduleFile);

        Model<?> model1 = Model.load("external.Class1");
        assertNotNull(model1);

        Class class1 = model1.type;
        assertNotNull(class1);

        ClassLoader loader1 = class1.getClassLoader();
        assertNotNull(loader1);

        Object object1 = I.make(class1);
        assertNotNull(object1);

        // reload
        registry.load(moduleFile);

        Model<?> model2 = Model.load("external.Class1");
        assertNotNull(model2);

        Class class2 = model2.type;
        assertNotNull(class2);

        ClassLoader loader2 = class2.getClassLoader();
        assertNotNull(loader2);

        Object object2 = I.make(class2);
        assertNotNull(object2);

        // check
        assertNotSame(model1, model2);
        assertNotSame(class1, class2);
        assertNotSame(loader1, loader2);
        assertNotSame(object1, object2);
    }

    /**
     * Cached singleton class's identity check.
     */
    @Test
    public void testClassCacheForSingleton() {
        File moduleFile = registry.dir;

        registry.load(moduleFile);

        Model<?> model1 = Model.load("external.SingletonClass");
        assertNotNull(model1);

        Class class1 = model1.type;
        assertNotNull(class1);

        Object object1a = I.make(class1);
        assertNotNull(object1a);

        Object object1b = I.make(class1);
        assertNotNull(object1b);

        // check singleton
        assertEquals(object1a, object1b);

        // reload
        registry.load(moduleFile);

        Model<?> model2 = Model.load("external.SingletonClass");
        assertNotNull(model2);

        Class class2 = model2.type;
        assertNotNull(class2);

        Object object2a = I.make(class2);
        assertNotNull(object2a);

        Object object2b = I.make(class2);
        assertNotNull(object2b);

        // check singleton
        assertEquals(object2a, object2b);

        // check old class
        Object object3 = I.make(class1);
        assertNotSame(object1a, object3);
        assertNotSame(object2a, object3);
    }

    /**
     * Class as reference key will be removed automatically.
     */
    @Test
    public void testClassCacheInMapKeyReference() {
        File moduleFile = registry.dir;

        registry.load(moduleFile);

        Model<?> model = Model.load("external.Class1");
        assertNotNull(model);

        Class clazz = model.type;
        assertNotNull(clazz);

        // create module aware map
        Map<Class, Object> cache = Modules.aware(new HashMap());
        cache.put(clazz, "1");
        assertTrue(cache.containsKey(clazz));

        // unload module
        registry.unload(moduleFile);
        assertFalse(cache.containsKey(clazz));
    }
}
