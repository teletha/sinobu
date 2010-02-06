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
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Rule;
import org.junit.Test;

import ezbean.ClassLoadListener;
import ezbean.I;
import ezbean.Manageable;
import ezbean.Singleton;
import ezbean.sample.MarkerInterface1;
import ezbean.sample.MarkerInterface2;
import ezbean.sample.RuntimeAnnotation1;

/**
 * DOCUMENT.
 * 
 * @version 2008/06/20 15:43:04
 */
public class ClassLoadListenerTest {

    @Rule
    public static ModuleTestRule registry = new ModuleTestRule();

    /** The listener. */
    private MarkerListener markerListener = I.make(MarkerListener.class);

    /** The listener. */
    private FrozenListener frozenListener = I.make(FrozenListener.class);

    /** The listener. */
    private Listener listener = I.make(Listener.class);

    /** The listener. */
    private AnnotationListener annotationListener = I.make(AnnotationListener.class);

    /** The listener. */
    private ExtendedListener extendedListener = I.make(ExtendedListener.class);

    /**
     * Clea Up.
     */
    @After
    public void cleanup() {
        markerListener.clear();
        registry.unload(markerListener.getClass());

        frozenListener.clear();
        registry.unload(frozenListener.getClass());

        listener.clear();
        registry.unload(listener.getClass());
    }

    /**
     * Test method for {@link ezbean.ClassLoadListener#load(java.lang.Class)}.
     */
    @Test
    public void testLoad1() {
        File moduleFile = registry.jar;

        registry.load(markerListener.getClass());

        // load module
        registry.load(moduleFile);
        assertEquals(3, markerListener.loaded.size());
        assertEquals(0, markerListener.unloaded.size());

        // clear
        markerListener.clear();

        // unload module
        registry.unload(moduleFile);
        assertEquals(3, markerListener.unloaded.size());
        assertEquals(0, markerListener.loaded.size());
    }

    /**
     * Test method for {@link ezbean.ClassLoadListener#load(java.lang.Class)}.
     */
    @Test
    public void testLoad2() {
        File moduleFile = registry.zip;

        registry.load(listener.getClass());

        // load module
        registry.load(moduleFile);
        assertEquals(12, listener.loaded.size());
        assertEquals(0, listener.unloaded.size());

        // clear
        listener.clear();

        // unload module
        registry.unload(moduleFile);
        assertEquals(12, listener.unloaded.size());
        assertEquals(0, listener.loaded.size());
    }

    /**
     * Test method for {@link ezbean.ClassLoadListener#load(java.lang.Class)}.
     */
    @Test
    public void testLoad3() {
        File moduleFile = registry.zip;

        registry.load(frozenListener.getClass());

        // load module
        registry.load(moduleFile);
        assertEquals(2, frozenListener.loaded.size());
        assertEquals(0, frozenListener.unloaded.size());

        // clear
        frozenListener.clear();

        // unload module
        registry.unload(moduleFile);
        assertEquals(2, frozenListener.unloaded.size());
        assertEquals(0, frozenListener.loaded.size());
    }

    /**
     * Test method for {@link ezbean.ClassLoadListener#load(java.lang.Class)}.
     */
    @Test
    public void testLoad7() {
        File moduleFile = registry.jar;

        registry.load(markerListener.getClass());

        // load module
        registry.load(moduleFile);
        assertEquals(3, markerListener.loaded.size());
        assertEquals(0, markerListener.unloaded.size());

        // clear
        markerListener.clear();

        // unload module
        registry.unload(moduleFile);
        assertEquals(3, markerListener.unloaded.size());
        assertEquals(0, markerListener.loaded.size());
    }

    /**
     * Test method for {@link ezbean.ClassLoadListener#load(java.lang.Class)}.
     */
    @Test
    public void testLoad8() {
        File moduleFile = registry.jar;

        registry.load(frozenListener.getClass());

        // load module
        registry.load(moduleFile);
        assertEquals(2, frozenListener.loaded.size());
        assertEquals(0, frozenListener.unloaded.size());

        // clear
        frozenListener.clear();

        // unload module
        registry.unload(moduleFile);
        assertEquals(2, frozenListener.unloaded.size());
        assertEquals(0, frozenListener.loaded.size());
    }

    /**
     * Test method for {@link ezbean.ClassLoadListener#load(java.lang.Class)}.
     */
    @Test
    public void testLoad9() {
        File moduleFile = registry.jar;

        registry.load(listener.getClass());

        // load module
        registry.load(moduleFile);
        assertEquals(12, listener.loaded.size());
        assertEquals(0, listener.unloaded.size());

        // clear
        listener.clear();

        // unload module
        registry.unload(moduleFile);
        assertEquals(12, listener.unloaded.size());
        assertEquals(0, listener.loaded.size());
    }

    /**
     * Test method for {@link ezbean.ClassLoadListener#load(java.lang.Class)}.
     */
    @Test
    public void testLoad10() {
        File moduleFile = registry.zip;

        registry.load(annotationListener.getClass());

        // load module
        registry.load(moduleFile);
        assertEquals(4, annotationListener.loaded.size());
        assertEquals(0, annotationListener.unloaded.size());

        // clear
        annotationListener.clear();

        // unload module
        registry.unload(moduleFile);
        assertEquals(4, annotationListener.unloaded.size());
        assertEquals(0, annotationListener.loaded.size());
    }

    @Test
    public void testLoad11() {
        File moduleFile = registry.zip;

        registry.load(extendedListener.getClass());

        // load module
        registry.load(moduleFile);
        assertEquals(3, extendedListener.loaded.size());
        assertEquals(0, extendedListener.unloaded.size());

        // clear
        extendedListener.clear();

        // unload module
        registry.unload(moduleFile);
        assertEquals(3, extendedListener.unloaded.size());
        assertEquals(0, extendedListener.loaded.size());
    }

    /**
     * DOCUMENT.
     * 
     * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
     * @version $ Id: Listener.java,v 1.0 2007/03/08 18:11:15 Teletha Exp $
     */
    @Manageable(lifestyle = Singleton.class)
    private static class Listener implements ClassLoadListener {

        private List<Class> loaded = new ArrayList();

        private List<Class> unloaded = new ArrayList();

        /**
         * @see ezbean.ClassLoadListener#load(java.lang.Class)
         */
        public void load(Class clazz) {
            loaded.add(clazz);
        }

        /**
         * @see ezbean.ClassLoadListener#unload(java.lang.Class)
         */
        public void unload(Class clazz) {
            unloaded.add(clazz);
        }

        private void clear() {
            loaded.clear();
            unloaded.clear();
        }
    }

    /**
     * DOCUMENT.
     * 
     * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
     * @version $ Id: MarkerListener.java,v 1.0 2007/01/29 3:35:54 Teletha Exp $
     */
    @Manageable(lifestyle = Singleton.class)
    private static class MarkerListener implements ClassLoadListener<MarkerInterface1> {

        private List<Class> loaded = new ArrayList();

        private List<Class> unloaded = new ArrayList();

        /**
         * @see ezbean.ClassLoadListener#load(java.lang.Class)
         */
        public void load(Class clazz) {
            loaded.add(clazz);
        }

        /**
         * @see ezbean.ClassLoadListener#unload(java.lang.Class)
         */
        public void unload(Class clazz) {
            unloaded.add(clazz);
        }

        private void clear() {
            loaded.clear();
            unloaded.clear();
        }
    }

    /**
     * DOCUMENT.
     * 
     * @author <a href="mailto:Teletha.T@gmail.com">Teletha Testarossa</a>
     * @version $ Id: FrozenListener.java,v 1.0 2007/01/29 3:35:54 Teletha Exp $
     */
    @Manageable(lifestyle = Singleton.class)
    private static class FrozenListener implements ClassLoadListener<MarkerInterface2> {

        private List<Class> loaded = new ArrayList();

        private List<Class> unloaded = new ArrayList();

        /**
         * @see ezbean.ClassLoadListener#load(java.lang.Class)
         */
        public void load(Class<MarkerInterface2> clazz) {
            loaded.add(clazz);
        }

        /**
         * @see ezbean.ClassLoadListener#unload(java.lang.Class)
         */
        public void unload(Class<MarkerInterface2> clazz) {
            unloaded.add(clazz);
        }

        private void clear() {
            loaded.clear();
            unloaded.clear();
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2007/11/28 3:44:41
     */
    @Manageable(lifestyle = Singleton.class)
    private static class AnnotationListener implements ClassLoadListener<RuntimeAnnotation1> {

        private List<Class> loaded = new ArrayList();

        private List<Class> unloaded = new ArrayList();

        /**
         * @see ezbean.ClassLoadListener#load(java.lang.Class)
         */
        public void load(Class<RuntimeAnnotation1> clazz) {
            loaded.add(clazz);
            assertNotNull(clazz.getAnnotation(RuntimeAnnotation1.class));
        }

        /**
         * @see ezbean.ClassLoadListener#unload(java.lang.Class)
         */
        public void unload(Class<RuntimeAnnotation1> clazz) {
            unloaded.add(clazz);
            assertNotNull(clazz.getAnnotation(RuntimeAnnotation1.class));
        }

        private void clear() {
            loaded.clear();
            unloaded.clear();
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:49:25
     */
    private static abstract class ExtendableListener<T> implements ClassLoadListener<T> {

        protected List<Class> loaded = new ArrayList();

        protected List<Class> unloaded = new ArrayList();

        /**
         * @see ezbean.ClassLoadListener#load(java.lang.Class)
         */
        public void load(Class clazz) {
            loaded.add(clazz);
        }

        /**
         * @see ezbean.ClassLoadListener#unload(java.lang.Class)
         */
        public void unload(Class clazz) {
            unloaded.add(clazz);
        }

        protected void clear() {
            loaded.clear();
            unloaded.clear();
        }
    }

    /**
     * DOCUMENT.
     * 
     * @version 2008/06/20 12:50:21
     */
    @Manageable(lifestyle = Singleton.class)
    private static class ExtendedListener extends ExtendableListener<MarkerInterface1> {
    }
}
