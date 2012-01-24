/*
 * Copyright (C) 2012 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import testament.PrivateModule;

import kiss.sample.MarkerInterface1;
import kiss.sample.MarkerInterface2;
import kiss.sample.RuntimeAnnotation1;

/**
 * @version 2011/04/09 7:09:57
 */
public class ClassListenerTest {

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
        modules.load(module.path);

        listener.assertClass(3, 0);

        // unload module
        modules.unload(module.path);

        listener.assertClass(3, 3);
    }

    @Test
    public void awareInterface2() throws Exception {
        Interface2Listener listener = I.make(Interface2Listener.class);

        // load listener
        modules.load(Interface2Listener.class);

        // load module
        modules.load(module.path);

        listener.assertClass(1, 0);

        // unload module
        modules.unload(module.path);

        listener.assertClass(1, 1);
    }

    @Test
    public void awareAnnotation() throws Exception {
        AnnotationListener listener = I.make(AnnotationListener.class);

        // load listener
        modules.load(AnnotationListener.class);

        // load module
        modules.load(module.path);

        listener.assertClass(3, 0);

        // unload module
        modules.unload(module.path);

        listener.assertClass(3, 3);
    }

    @Test
    public void awareAllClass() throws Exception {
        ClassLoadListener listener = I.make(ClassLoadListener.class);

        // load listener
        modules.load(ClassLoadListener.class);

        // load module
        modules.load(module.path);

        listener.assertClass(10, 0);

        // unload module
        modules.unload(module.path);

        listener.assertClass(10, 10);
    }

    /**
     * @version 2010/11/13 23:33:35
     */
    @Manageable(lifestyle = Singleton.class)
    private abstract static class AbstractCounter<T> implements ClassListener<T> {

        private int loaded = 0;

        private int unloaded = 0;

        /**
         * @see kiss.ClassListener#load(java.lang.Class)
         */
        public void load(Class<T> clazz) {
            loaded++;
        }

        /**
         * @see kiss.ClassListener#unload(java.lang.Class)
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
            assert loaded == this.loaded;
            assert unloaded == this.unloaded;
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
    private static class ClassLoadListener implements ClassListener {

        private int loaded = 0;

        private int unloaded = 0;

        /**
         * @see kiss.ClassListener#load(java.lang.Class)
         */
        public void load(Class clazz) {
            loaded++;
        }

        /**
         * @see kiss.ClassListener#unload(java.lang.Class)
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
            assert loaded == this.loaded;
            assert unloaded == this.unloaded;
        }
    }
}
