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

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Rule;
import org.junit.Test;

import ezbean.module.external.AnnotatedClass1;
import ezbean.module.external.AnnotatedClass2;
import ezbean.module.external.AnnotatedClass3;
import ezbean.module.external.AnnotatedExtendedClass1;
import ezbean.module.external.ExtendedClass1;
import ezbean.module.external.ExtendedClass2;
import ezbean.module.external.ExtendedClass3;
import ezbean.module.external.ExtendedClass4;
import ezbean.module.external.ExtendedClass5;
import ezbean.module.external.SingletonClass;
import ezbean.sample.ClassAnnotation;
import ezbean.sample.MarkerInterface1;
import ezbean.sample.MarkerInterface2;
import ezbean.sample.RuntimeAnnotation1;
import ezbean.sample.RuntimeAnnotation2;
import ezbean.sample.SourceAnnotation;
import ezunit.PrivateModule;

/**
 * @version 2010/11/14 21:36:46
 */
public class ModuleTest {

    @Rule
    public static PrivateModule external = new PrivateModule("module/external", true, false);

    /**
     * Test method for {@link ezbean.Module#getModuleFile()}.
     */
    @Test
    public void testGetModuleFile() throws Exception {
        Module module = new Module(external.modulePath);
        assertNotNull(module);
        assertEquals(external.modulePath, module.path);
    }

    /**
     * Ezbean module has a {@link Module}.
     */
    @Test
    public void testModuleClassLoader() throws Exception {
        Module module = new Module(external.modulePath);
        assertNotNull(module);
        assertNotSame(I.loader, module);
    }

    /**
     * Test method for {@link ezbean.Module#findAll(java.lang.Class)}.
     */
    @Test
    public void testFindProviders1() throws Exception {
        Module module = new Module(external.modulePath);
        assertNotNull(module);

        List<Class<MarkerInterface1>> providers = module.find(MarkerInterface1.class, false);
        assertNotNull(providers);
        assertEquals(3, providers.size());

        // collect names to assert
        check(providers, AnnotatedExtendedClass1.class, ExtendedClass3.class, ExtendedClass5.class);
    }

    /**
     * Test method for {@link ezbean.Module#findAll(java.lang.Class)}.
     */
    @Test
    public void testFindProviders2() throws Exception {
        Module module = new Module(external.modulePath);
        assertNotNull(module);

        List<Class<MarkerInterface2>> providers = module.find(MarkerInterface2.class, false);
        assertNotNull(providers);
        assertEquals(1, providers.size());

        // collect names to assert
        check(providers, ExtendedClass4.class);
    }

    /**
     * Find all classes which extends Object class. In other words, it is all service provider
     * classes.
     */
    @Test
    public void testFindProviders3() throws Exception {
        Module module = new Module(external.modulePath);
        assertNotNull(module);

        List<Class<Object>> providers = module.find(Object.class, false);
        assertNotNull(providers);
        assertEquals(10, providers.size());

        // collect names to assert
        check(providers, AnnotatedClass1.class, AnnotatedClass2.class, AnnotatedClass3.class, AnnotatedExtendedClass1.class, ExtendedClass1.class, ExtendedClass2.class, ExtendedClass3.class, ExtendedClass4.class, ExtendedClass5.class, SingletonClass.class);
    }

    /**
     * Find annotated classes.
     */
    @Test
    public void testFindProviders4() throws Exception {
        Module module = new Module(external.modulePath);
        assertNotNull(module);

        List<Class<RuntimeAnnotation1>> providers = module.find(RuntimeAnnotation1.class, false);
        assertNotNull(providers);
        assertEquals(3, providers.size());

        // collect names to assert
        check(providers, AnnotatedClass1.class, AnnotatedClass3.class, AnnotatedExtendedClass1.class);
    }

    /**
     * Find annotated classes.
     */
    @Test
    public void testFindProviders5() throws Exception {
        Module module = new Module(external.modulePath);
        assertNotNull(module);

        List<Class<RuntimeAnnotation2>> providers = module.find(RuntimeAnnotation2.class, false);
        assertNotNull(providers);
        assertEquals(2, providers.size());

        // collect names to assert
        check(providers, AnnotatedClass2.class, AnnotatedClass3.class);
    }

    /**
     * Find annotated classes.
     */
    @Test
    public void testFindProviders6() throws Exception {
        Module module = new Module(external.modulePath);
        assertNotNull(module);

        List<Class<SourceAnnotation>> providers = module.find(SourceAnnotation.class, false);
        assertNotNull(providers);
        assertEquals(0, providers.size());
    }

    /**
     * Find annotated classes.
     */
    @Test
    public void testFindProviders7() throws Exception {
        Module module = new Module(external.modulePath);
        assertNotNull(module);

        List<Class<ClassAnnotation>> providers = module.find(ClassAnnotation.class, false);
        assertNotNull(providers);
        assertEquals(0, providers.size());
    }

    /**
     * Test that the inline module isn't scanned.
     */
    @Test
    public void testModuleInModule() throws Exception {
        Module module = new Module(new File("src/test/resources/ezbean/inline.zip").toPath());
        assertNotNull(module);

        List<Class<Object>> providers = module.find(Object.class, false);
        assertNotNull(providers);
        assertEquals(0, providers.size());
    }

    /**
     * Helper method to assert classes.
     * 
     * @param providers
     * @param classes
     */
    private void check(List providers, Class... classes) {
        // collect names to assert
        Set names = new HashSet();

        for (Object provider : providers) {
            names.add(((Class) provider).getName());
        }

        for (Class clazz : classes) {
            assertTrue(names.contains(external.convert(clazz).getName()));
        }
    }
}
