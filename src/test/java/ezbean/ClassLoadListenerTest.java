/*
 * Copyright (C) 2011 Nameless Production Committee.
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
package ezbean;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ezbean.sample.MarkerInterface1;
import ezbean.sample.MarkerInterface2;
import ezbean.sample.RuntimeAnnotation1;
import ezunit.PrivateModule;

/**
 * @version 2010/11/15 0:56:29
 */
public class ClassLoadListenerTest {

    @Rule
    public static final PrivateModule module = new PrivateModule("module/external");

    /** The clean and empty module repository for test. */
    private Modules modules;

    @Before
    public void before() {
        modules = new Modules();
        modules.modules.clear();
        modules.types.clear();
    }

    @Test
    public void awareInterface1() throws Exception {
        Interface1Listener listener = I.make(Interface1Listener.class);

        // load listener
        modules.load(Interface1Listener.class);

        // load module
        modules.load(module.module);

        listener.assertClass(3, 0);

        // unload module
        modules.unload(module.module);

        listener.assertClass(3, 3);
    }

    @Test
    public void awareInterface2() throws Exception {
        Interface2Listener listener = I.make(Interface2Listener.class);

        // load listener
        modules.load(Interface2Listener.class);

        // load module
        modules.load(module.module);

        listener.assertClass(1, 0);

        // unload module
        modules.unload(module.module);

        listener.assertClass(1, 1);
    }

    @Test
    public void awareAnnotation() throws Exception {
        AnnotationListener listener = I.make(AnnotationListener.class);

        // load listener
        modules.load(AnnotationListener.class);

        // load module
        modules.load(module.module);

        listener.assertClass(3, 0);

        // unload module
        modules.unload(module.module);

        listener.assertClass(3, 3);
    }

    @Test
    public void awareAllClass() throws Exception {
        ClassListener listener = I.make(ClassListener.class);

        // load listener
        modules.load(ClassListener.class);

        // load module
        modules.load(module.module);

        listener.assertClass(10, 0);

        // unload module
        modules.unload(module.module);

        listener.assertClass(10, 10);
    }

    /**
     * @version 2010/11/13 23:33:35
     */
    @Manageable(lifestyle = Singleton.class)
    private abstract static class AbstractCounter<T> implements ClassLoadListener<T> {

        private int loaded = 0;

        private int unloaded = 0;

        /**
         * @see ezbean.ClassLoadListener#load(java.lang.Class)
         */
        public void load(Class<T> clazz) {
            loaded++;
        }

        /**
         * @see ezbean.ClassLoadListener#unload(java.lang.Class)
         */
        public void unload(Class<T> clazz) {
            unloaded++;
        }

        /**
         * Helper method to assert class loading and unloading.
         * 
         * @param loaded
         * @param unloaded
         */
        protected void assertClass(int loaded, int unloaded) {
            assertEquals(loaded, this.loaded);
            assertEquals(unloaded, this.unloaded);
        }
    }

    /**
     * @version 2010/11/13 23:28:29
     */
    private static class Interface1Listener extends AbstractCounter<MarkerInterface1> {
    }

    /**
     * @version 2010/11/13 23:28:29
     */
    private static class Interface2Listener extends AbstractCounter<MarkerInterface2> {
    }

    /**
     * @version 2010/11/13 23:28:29
     */
    private static class AnnotationListener extends AbstractCounter<RuntimeAnnotation1> {
    }

    /**
     * @version 2010/11/13 23:33:35
     */
    @Manageable(lifestyle = Singleton.class)
    private static class ClassListener implements ClassLoadListener {

        private int loaded = 0;

        private int unloaded = 0;

        /**
         * @see ezbean.ClassLoadListener#load(java.lang.Class)
         */
        public void load(Class clazz) {
            loaded++;
        }

        /**
         * @see ezbean.ClassLoadListener#unload(java.lang.Class)
         */
        public void unload(Class clazz) {
            unloaded++;
        }

        /**
         * Helper method to assert class loading and unloading.
         * 
         * @param loaded
         * @param unloaded
         */
        protected void assertClass(int loaded, int unloaded) {
            assertEquals(loaded, this.loaded);
            assertEquals(unloaded, this.unloaded);
        }
    }
}
