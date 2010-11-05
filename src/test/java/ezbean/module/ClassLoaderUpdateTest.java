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

import org.junit.Rule;
import org.junit.Test;

import ezbean.ClassLoadListener;
import ezbean.I;
import ezbean.Manageable;
import ezbean.ModuleTestRule;
import ezbean.Singleton;

/**
 * DOCUMENT.
 * 
 * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
 * @version $ Id: ClassLoaderUpdateTest.java,v 1.0 2007/02/04 1:06:48 Teletha Exp $
 */
public class ClassLoaderUpdateTest {

    @Rule
    public static ModuleTestRule registry = new ModuleTestRule();

    /**
     * Check whether a class loader of the module class is changed or not when module reloading.
     */
    @Test
    public void test1() {
        Checker checker = I.make(Checker.class);
        registry.load(checker.getClass());

        // load
        registry.load(registry.dir);

        // store
        ClassLoader loader = checker.loader;

        // clear
        checker.loader = null;

        // reload
        registry.load(registry.dir);

        // assert classloader
        assertNotSame(loader, checker.loader);

        // cleanup
        checker.loader = null;
        registry.unload(checker.getClass());
    }

    /**
     * Check whether a class loader of the module class is changed or not when module reloading. For
     * Jar.
     */
    @Test
    public void test2() {
        Checker checker = I.make(Checker.class);
        registry.load(checker.getClass());

        // load
        registry.load(registry.jar);

        // store
        ClassLoader loader = checker.loader;

        // clear
        checker.loader = null;

        // reload
        registry.load(registry.jar);

        // assert classloader
        assertNotSame(loader, checker.loader);

        // cleanup
        checker.loader = null;
        registry.unload(checker.getClass());
    }

    /**
     * Check whether a class loader of the module class is changed or not when module reloading. For
     * Zip.
     */
    @Test
    public void test3() {
        Checker checker = I.make(Checker.class);
        registry.load(checker.getClass());

        // load
        registry.load(registry.zip);

        // store
        ClassLoader loader = checker.loader;

        // clear
        checker.loader = null;

        // reload
        registry.load(registry.zip);

        // assert classloader
        assertNotSame(loader, checker.loader);

        // cleanup
        checker.loader = null;
        registry.unload(checker.getClass());
    }

    /**
     * Check whether a class loader of the module class is changed or not when module reloading. For
     * Zip.
     */
    @Test
    public void test4() {
        Creator creator = I.make(Creator.class);
        registry.load(creator.getClass());

        // load
        registry.load(registry.zip);

        // store
        Object object1 = creator.object;
        assertNotNull(object1);

        // clear
        creator.object = null;

        // reload
        registry.load(registry.zip);

        // assert classloader
        Object object2 = creator.object;
        assertNotNull(object2);

        // assert
        assertNotSame(object1, object2);
        assertNotSame(object1.getClass(), object2.getClass());
        assertNotSame(object1.getClass().getClassLoader(), object2.getClass().getClassLoader());

        // clean up
        registry.unload(registry.zip);
        registry.unload(creator.getClass());
        creator.object = null;
    }

    /**
     * DOCUMENT.
     * 
     * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
     * @version $ Id: Assert.java,v 1.0 2007/02/04 1:10:39 Teletha Exp $
     */
    @Manageable(lifestyle = Singleton.class)
    private static class Checker implements ClassLoadListener<Object> {

        private ClassLoader loader;

        /**
         * @see ezbean.ClassLoadListener#load(java.lang.Class)
         */
        public void load(Class clazz) {
            if (loader == null) {
                loader = clazz.getClassLoader();
            } else {
                assertSame(loader, clazz.getClassLoader());
            }
        }

        /**
         * @see ezbean.ClassLoadListener#unload(java.lang.Class)
         */
        public void unload(Class clazz) {
            if (loader != null) {
                assertSame(loader, clazz.getClassLoader());
            }
        }
    }

    /**
     * DOCUMENT.
     * 
     * @author <a href="mailto:Teletha.NPC@gmail.com">Teletha Testarossa</a>
     * @version $ Id: Creator.java,v 1.0 2007/03/30 20:55:09 Teletha Exp $
     */
    @Manageable(lifestyle = Singleton.class)
    private static final class Creator implements ClassLoadListener<Object> {

        private Object object;

        /**
         * @see ezbean.ClassLoadListener#load(java.lang.Class)
         */
        public void load(Class clazz) {
            if (object == null) {
                object = I.make(clazz);
            }
        }

        /**
         * @see ezbean.ClassLoadListener#unload(java.lang.Class)
         */
        public void unload(Class clazz) {
        }
    }
}
