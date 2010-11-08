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
package ezbean;

import static org.junit.Assert.*;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import ezbean.sample.ClassAnnotation;
import ezbean.sample.MarkerInterface1;
import ezbean.sample.MarkerInterface2;
import ezbean.sample.RuntimeAnnotation1;
import ezbean.sample.RuntimeAnnotation2;
import ezbean.sample.SourceAnnotation;

/**
 * DOCUMENT.
 * 
 * @version 2008/11/22 17:28:56
 */
public class ModuleTest {

    @Rule
    public static ModuleTestRule registry = new ModuleTestRule();

    /**
     * Test method for {@link ezbean.Module#getModuleFile()}.
     */
    @Test
    public void testGetModuleFile() throws Exception {
        File moduleFile = registry.dir;

        Module module = new Module(moduleFile);
        assertNotNull(module);
        assertEquals(moduleFile, module.moduleFile);
    }

    /**
     * Ezbean module has a {@link Module}.
     */
    @Test
    public void testEzbeanModuleLoader() throws Exception {
        File moduleFile = registry.dir;

        Module module = new Module(moduleFile);
        assertNotNull(module);
        assertNotSame(I.loader, module);
    }

    /**
     * Test method for {@link ezbean.Module#findAll(java.lang.Class)}.
     */
    @Test
    public void testFindProviders1() throws Exception {
        File moduleFile = registry.zip;

        Module module = new Module(moduleFile);
        assertNotNull(module);

        Class interface1 = module.loadClass("external.Interface1");

        List<Class<? extends MarkerInterface1>> providers = module.find(interface1, false);
        assertNotNull(providers);
        assertEquals(3, providers.size());

        // collect names to assert
        Set names = new HashSet();

        for (Class provider : providers) {
            names.add(provider.getName());
        }
        assertTrue(names.contains("external.extend.AnnotatedExtendedClass1"));
        assertTrue(names.contains("external.extend.ExtendedClass4"));
        assertTrue(names.contains("external.extend.ExtendedClass6"));
    }

    /**
     * Test method for {@link ezbean.Module#findAll(java.lang.Class)}.
     */
    @Test
    public void testFindProviders2() throws Exception {
        File moduleFile = registry.jar;

        Module module = new Module(moduleFile);
        assertNotNull(module);

        List<Class<MarkerInterface2>> providers = module.find(MarkerInterface2.class, false);
        assertNotNull(providers);
        assertEquals(2, providers.size());

        // collect names to assert
        Set names = new HashSet();

        for (Class provider : providers) {
            names.add(provider.getName());
        }
        assertTrue(names.contains("external.extend.ExtendedClass5"));
        assertTrue(names.contains("external.extend.ExtendedClass6"));
    }

    /**
     * Find all classes which extends Object class. In other words, it is all service provider
     * classes.
     */
    @Test
    public void testFindProviders3() throws Exception {
        File moduleFile = registry.zip;

        Module module = new Module(moduleFile);
        assertNotNull(module);

        List<Class<Object>> providers = module.find(Object.class, false);
        assertNotNull(providers);
        assertEquals(12, providers.size());

        // collect names to assert
        Set names = new HashSet();

        for (Class provider : providers) {
            names.add(provider.getName());
        }
        assertTrue(names.contains("external.extend.AnnotatedClass1"));
        assertTrue(names.contains("external.extend.AnnotatedClass2"));
        assertTrue(names.contains("external.extend.AnnotatedClass5"));
        assertTrue(names.contains("external.extend.AnnotatedClass6"));
        assertTrue(names.contains("external.extend.AnnotatedExtendedClass1"));
        assertTrue(names.contains("external.extend.ExtendedClass1"));
        assertTrue(names.contains("external.extend.ExtendedClass2"));
        assertTrue(names.contains("external.extend.ExtendedClass3"));
        assertTrue(names.contains("external.extend.ExtendedClass4"));
        assertTrue(names.contains("external.extend.ExtendedClass5"));
        assertTrue(names.contains("external.extend.ExtendedClass6"));
        assertTrue(names.contains("external.SingletonClass"));
    }

    /**
     * Find annotated classes.
     */
    @Test
    public void testFindProviders4() throws Exception {
        File moduleFile = registry.zip;

        Module module = new Module(moduleFile);
        assertNotNull(module);

        // load service proider interface
        Class annotationClass = RuntimeAnnotation1.class;
        assertNotNull(annotationClass);

        List<Class<?>> providers = module.find(annotationClass, false);
        assertNotNull(providers);
        assertEquals(4, providers.size());

        // collect names to assert
        Set names = new HashSet();

        for (Class provider : providers) {
            names.add(provider.getName());
        }
        assertTrue(names.contains("external.extend.AnnotatedClass1"));
        assertTrue(names.contains("external.extend.AnnotatedClass5"));
        assertTrue(names.contains("external.extend.AnnotatedClass6"));
        assertTrue(names.contains("external.extend.AnnotatedExtendedClass1"));
    }

    /**
     * Find annotated classes.
     */
    @Test
    public void testFindProviders5() throws Exception {
        File moduleFile = registry.zip;

        Module module = new Module(moduleFile);
        assertNotNull(module);

        // load service proider interface
        Class annotationClass = RuntimeAnnotation2.class;
        assertNotNull(annotationClass);

        List<Class<?>> providers = module.find(annotationClass, false);
        assertNotNull(providers);
        assertEquals(2, providers.size());

        // collect names to assert
        Set names = new HashSet();

        for (Class provider : providers) {
            names.add(provider.getName());
        }
        assertTrue(names.contains("external.extend.AnnotatedClass2"));
        assertTrue(names.contains("external.extend.AnnotatedClass5"));
    }

    /**
     * Find annotated classes.
     */
    @Test
    public void testFindProviders6() throws Exception {
        File moduleFile = registry.zip;

        Module module = new Module(moduleFile);
        assertNotNull(module);

        // load service proider interface
        Class annotationClass = ClassAnnotation.class;
        assertNotNull(annotationClass);

        List<Class<?>> providers = module.find(annotationClass, false);
        assertNotNull(providers);
        assertEquals(0, providers.size());
    }

    /**
     * Find annotated classes.
     */
    @Test
    public void testFindProviders7() throws Exception {
        File moduleFile = registry.zip;

        Module module = new Module(moduleFile);
        assertNotNull(module);

        // load service proider interface
        Class annotationClass = SourceAnnotation.class;
        assertNotNull(annotationClass);

        List<Class<?>> providers = module.find(annotationClass, false);
        assertNotNull(providers);
        assertEquals(0, providers.size());
    }

    /**
     * Test that the inline module isn't scanned.
     */
    @Test
    public void testModuleInModule() throws Exception {
        File moduleFile = registry.nest;
        Module module = new Module(moduleFile);
        assertNotNull(module);

        List<Class<Object>> providers = module.find(Object.class, false);
        assertNotNull(providers);
        assertEquals(0, providers.size());
    }
}
