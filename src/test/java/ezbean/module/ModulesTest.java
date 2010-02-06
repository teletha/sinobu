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
import java.util.List;

import org.junit.Rule;
import org.junit.Test;

import ezbean.sample.MarkerInterface1;

/**
 * @version 2009/12/23 13:20:51
 */
public class ModulesTest {

    @Rule
    public static ModuleTestRule registry = new ModuleTestRule();

    /**
     * Test method for {@link ezbean.module.Modules#load(java.io.File, boolean)}.
     */
    @Test
    public void testLoad1() {
        assertEquals(0, registry.modules.size());
        registry.load(registry.dir);
        assertEquals(1, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#load(java.io.File, boolean)}.
     */
    @Test
    public void testLoad2() {
        assertEquals(0, registry.modules.size());
        registry.load(registry.jar);
        assertEquals(1, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#load(java.io.File, boolean)}.
     */
    @Test
    public void testLoad3() {
        assertEquals(0, registry.modules.size());
        registry.load(registry.zip);
        assertEquals(1, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#load(java.io.File, boolean)}.
     */
    @Test
    public void testLoad4() {
        assertEquals(0, registry.modules.size());
        registry.load(registry.dir);
        assertEquals(1, registry.modules.size());

        registry.load(registry.jar);
        assertEquals(2, registry.modules.size());

        registry.load(registry.zip);
        assertEquals(3, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#load(java.io.File, boolean)}.
     */
    @Test
    public void testLoad5() {
        assertEquals(0, registry.modules.size());
        registry.load((File) null);
        assertEquals(0, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#load(java.io.File, boolean)}.
     */
    @Test
    public void testLoad6() {
        assertEquals(0, registry.modules.size());
        registry.load(new File("not-exist"));
        assertEquals(0, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#load(java.io.File, boolean)}.
     */
    @Test
    public void testReload1() {
        assertEquals(0, registry.modules.size());
        registry.load(registry.dir);
        assertEquals(1, registry.modules.size());

        registry.load(registry.dir);
        assertEquals(1, registry.modules.size());

        registry.load(registry.dir);
        assertEquals(1, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#load(java.io.File, boolean)}.
     */
    @Test
    public void testReload2() {
        assertEquals(0, registry.modules.size());
        registry.load(registry.jar);
        assertEquals(1, registry.modules.size());

        registry.load(registry.jar);
        assertEquals(1, registry.modules.size());

        registry.load(registry.jar);
        assertEquals(1, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#load(java.io.File, boolean)}.
     */
    @Test
    public void testReload3() {
        assertEquals(0, registry.modules.size());
        registry.load(registry.zip);
        assertEquals(1, registry.modules.size());

        registry.load(registry.zip);
        assertEquals(1, registry.modules.size());

        registry.load(registry.zip);
        assertEquals(1, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#load(java.io.File, boolean)}.
     */
    @Test
    public void testReload4() {
        // as relative
        File moduleFileRelative = registry.dir;
        assertFalse(moduleFileRelative.isAbsolute());

        assertEquals(0, registry.modules.size());
        registry.load(moduleFileRelative);
        assertEquals(1, registry.modules.size());

        // as absolute
        File moduleFileAbsolute = moduleFileRelative.getAbsoluteFile();
        assertTrue(moduleFileAbsolute.isAbsolute());

        assertEquals(1, registry.modules.size());
        registry.load(moduleFileAbsolute);
        assertEquals(1, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#unload(java.io.File)}.
     */
    @Test
    public void testUnload1() {
        assertEquals(0, registry.modules.size());

        File moduleFile = registry.dir;
        registry.load(moduleFile);

        List<Module> modules = registry.modules;
        assertEquals(1, modules.size());
        assertEquals(moduleFile, modules.get(0).moduleFile);

        registry.unload(moduleFile);
        assertEquals(0, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#unload(java.io.File)}.
     */
    @Test
    public void testUnload2() {
        assertEquals(0, registry.modules.size());

        File moduleFile = registry.jar;
        registry.load(moduleFile);

        List<Module> modules = registry.modules;
        assertEquals(1, modules.size());
        assertEquals(moduleFile, modules.get(0).moduleFile);

        registry.unload(moduleFile);
        assertEquals(0, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#unload(java.io.File)}.
     */
    @Test
    public void testUnload3() {
        assertEquals(0, registry.modules.size());

        File moduleFile = registry.zip;
        registry.load(moduleFile);

        List<Module> modules = registry.modules;
        assertEquals(1, modules.size());
        assertEquals(moduleFile, modules.get(0).moduleFile);

        registry.unload(moduleFile);
        assertEquals(0, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#unload(java.io.File)}.
     */
    @Test
    public void testUnload4() {
        assertEquals(0, registry.modules.size());
        registry.unload((File) null);
        assertEquals(0, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.registry#unload(java.io.File)}.
     */
    @Test
    public void testUnload5() {
        assertEquals(0, registry.modules.size());
        registry.unload(new File("not-exist"));
        assertEquals(0, registry.modules.size());
    }

    /**
     * Test method for {@link ezbean.module.Module#findAll(java.lang.Class)}.
     */
    @Test
    public void testDupulicateClassLoading() {
        File moduleFile1 = registry.dir;

        registry.load(moduleFile1);
        Module module1 = registry.modules.get(0);
        List<Class<MarkerInterface1>> providers1 = module1.find(MarkerInterface1.class, false);

        // assert class loader
        // all service providers should been loaded by first module
        for (Class provider : providers1) {
            assertEquals(module1.moduleLoader, provider.getClassLoader());
        }

        // load another module which content is same
        File moduleFile2 = registry.jar;

        registry.load(moduleFile2);
        Module module2 = registry.modules.get(1);
        List<Class<MarkerInterface1>> providers2 = module2.find(MarkerInterface1.class, false);

        // assert class loader
        // all service providers should been loaded by first module
        for (Class provider : providers2) {
            assertEquals(module1.moduleLoader, provider.getClassLoader());
        }

        // unload first module
        registry.unload(moduleFile1);
        List<Class<MarkerInterface1>> providers3 = module2.find(MarkerInterface1.class, false);

        // assert class loader
        // all service providers should been loaded by second module
        for (Class provider : providers3) {
            assertEquals(module2.moduleLoader, provider.getClassLoader());
        }
    }
}
