/*
 * Copyright (C) 2014 Nameless Production Committee
 *
 * Licensed under the MIT License (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *          http://opensource.org/licenses/mit-license.php
 */
package kiss;

import kiss.sample.MarkerInterface1;
import kiss.sample.MarkerInterface2;
import kiss.sample.RuntimeAnnotation1;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import antibug.PrivateModule;

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
        listener.reset();

        // load listener
        modules.load(Interface1Listener.class, Disposable.NONE);

        // load module
        modules.load(module.path, "");

        listener.assertClass(3, 0);

        // unload module
        modules.unload(module.path);

        listener.assertClass(3, 3);
    }

    @Test
    public void awareInterface2() throws Exception {
        Interface2Listener listener = I.make(Interface2Listener.class);
        listener.reset();

        // load listener
        modules.load(Interface2Listener.class, Disposable.NONE);

        // load module
        modules.load(module.path, "");

        listener.assertClass(1, 0);

        // unload module
        modules.unload(module.path);

        listener.assertClass(1, 1);
    }

    @Test
    public void awareAnnotation() throws Exception {
        AnnotationListener listener = I.make(AnnotationListener.class);
        listener.reset();

        // load listener
        modules.load(AnnotationListener.class, Disposable.NONE);

        // load module
        modules.load(module.path, "");

        listener.assertClass(3, 0);

        // unload module
        modules.unload(module.path);

        listener.assertClass(3, 3);
    }

    @Test
    public void awareAllClass() throws Exception {
        ClassLoadListener listener = I.make(ClassLoadListener.class);
        listener.reset();

        // load listener
        modules.load(ClassLoadListener.class, Disposable.NONE);

        // load module
        modules.load(module.path, "");

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
         * {@inheritDoc}
         */
        @Override
        public Disposable load(Class<T> clazz, Disposable disposer) {
            loaded++;

            return () -> {
                unloaded++;
            };
        }

        /**
         * Helper method to assert class loading and unloading.
         */
        protected void assertClass(int loaded, int unloaded) {
            assert loaded == this.loaded;
            assert unloaded == this.unloaded;
        }

        /**
         * <p>
         * Reset counter.
         * </p>
         */
        protected void reset() {
            loaded = 0;
            unloaded = 0;
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
         * {@inheritDoc}
         */
        @Override
        public Disposable load(Class clazz, Disposable disposer) {
            loaded++;

            return () -> {
                unloaded++;
            };
        }

        /**
         * Helper method to assert class loading and unloading.
         */
        protected void assertClass(int loaded, int unloaded) {
            assert loaded == this.loaded;
            assert unloaded == this.unloaded;
        }

        /**
         * <p>
         * Reset counter.
         * </p>
         */
        protected void reset() {
            loaded = 0;
            unloaded = 0;
        }
    }
}
